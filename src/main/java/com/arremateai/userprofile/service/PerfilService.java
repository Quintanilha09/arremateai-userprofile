package com.arremateai.userprofile.service;

import com.arremateai.userprofile.domain.Usuario;
import com.arremateai.userprofile.dto.AlterarSenhaRequest;
import com.arremateai.userprofile.dto.AtualizarPerfilRequest;
import com.arremateai.userprofile.dto.PerfilResponse;
import com.arremateai.userprofile.exception.BusinessException;
import com.arremateai.userprofile.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.userprofile.avatar-storage-location}")
    private String uploadDir;

    @Value("${app.userprofile.base-url}")
    private String baseUrl;

    public PerfilResponse buscarPerfil(UUID userId) {
        Usuario usuario = buscarPorId(userId);
        return mapToResponse(usuario);
    }

    public PerfilResponse atualizarPerfil(UUID userId, AtualizarPerfilRequest request) {
        Usuario usuario = buscarPorId(userId);

        if (request.cpf() != null && !request.cpf().isBlank()) {
            usuarioRepository.findByCpf(request.cpf()).ifPresent(outro -> {
                if (!outro.getId().equals(userId)) {
                    throw new BusinessException("CPF já está em uso por outro usuário");
                }
            });
        }

        usuario.setNome(request.nome());
        if (request.telefone() != null) {
            usuario.setTelefone(request.telefone());
        }
        if (request.cpf() != null) {
            usuario.setCpf(request.cpf());
        }

        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    public void alterarSenha(UUID userId, AlterarSenhaRequest request) {
        if (!request.senhasConferem()) {
            throw new BusinessException("Nova senha e confirmação não conferem");
        }

        Usuario usuario = buscarPorId(userId);

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new BusinessException("Usuário não possui senha cadastrada");
        }

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new BusinessException("Nova senha não pode ser igual à senha atual");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    public PerfilResponse uploadAvatar(UUID userId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Arquivo de avatar é obrigatório");
        }
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("Avatar não pode ultrapassar 5MB");
        }
        // Validação de extensão (defesa em profundidade)
        String originalName = arquivo.getOriginalFilename();
        if (originalName != null) {
            String lower = originalName.toLowerCase();
            if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png") && !lower.endsWith(".webp")) {
                throw new BusinessException("Extensão de arquivo não permitida");
            }
        }
        // Validação de magic bytes — verifica o conteúdo real do arquivo,
        // não o Content-Type enviado pelo cliente (que pode ser forjado)
        try {
            if (!isImagemValida(arquivo.getBytes())) {
                throw new BusinessException("Apenas imagens JPEG, PNG ou WebP são permitidas");
            }
        } catch (IOException e) {
            throw new BusinessException("Erro ao processar arquivo de avatar");
        }

        Usuario usuario = buscarPorId(userId);
        removerArquivoAnterior(usuario.getAvatarUrl());

        String nomeArquivo = gerarNomeArquivo(userId, arquivo.getOriginalFilename());
        salvarArquivo(arquivo, nomeArquivo);

        usuario.setAvatarUrl(baseUrl + "/api/perfil/avatar/arquivo/" + nomeArquivo);
        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    public PerfilResponse removerAvatar(UUID userId) {
        Usuario usuario = buscarPorId(userId);
        removerArquivoAnterior(usuario.getAvatarUrl());
        usuario.setAvatarUrl(null);
        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    public Path resolverCaminhoAvatar(String nomeArquivo) {
        Path uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolved = uploadDirectory.resolve(nomeArquivo).toAbsolutePath().normalize();
        if (!resolved.startsWith(uploadDirectory)) {
            throw new BusinessException("Caminho de arquivo inválido");
        }
        return resolved;
    }

    private Usuario buscarPorId(UUID userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    private String gerarNomeArquivo(UUID userId, String nomeOriginal) {
        String extensao = "";
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return "avatar_" + userId + "_" + System.currentTimeMillis() + extensao;
    }

    private void salvarArquivo(MultipartFile arquivo, String nomeArquivo) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path destino = dir.resolve(nomeArquivo).toAbsolutePath().normalize();
            if (!destino.startsWith(dir)) {
                throw new BusinessException("Tentativa de path traversal detectada");
            }
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar avatar");
        }
    }

    private void removerArquivoAnterior(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return;
        String prefixo = baseUrl + "/api/perfil/avatar/arquivo/";
        String nome = avatarUrl.startsWith(prefixo) ? avatarUrl.substring(prefixo.length()) : null;
        if (nome == null) return;
        try {
            Path arquivo = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(nome);
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
        }
    }

    /**
     * Valida o conteúdo real do arquivo verificando os magic bytes (assinatura binária).
     * Previne bypass via Content-Type forjado pelo cliente.
     *
     * JPEG: FF D8 FF
     * PNG:  89 50 4E 47 0D 0A 1A 0A
     * WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50 (RIFF....WEBP)
     */
    private boolean isImagemValida(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }
        // JPEG: começa com FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: começa com 89 50 4E 47 0D 0A 1A 0A
        if ((bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A
                && (bytes[6] & 0xFF) == 0x1A && bytes[7] == 0x0A) {
            return true;
        }
        // WebP: RIFF (bytes 0-3) + WEBP (bytes 8-11)
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46
                && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return true;
        }
        return false;
    }

    private PerfilResponse mapToResponse(Usuario usuario) {
        return new PerfilResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getCpf(),
                usuario.getTipo(),
                usuario.getAvatarUrl(),
                usuario.getAtivo(),
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
