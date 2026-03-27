package com.leilao.userprofile.service;

import com.leilao.userprofile.domain.Usuario;
import com.leilao.userprofile.dto.AlterarSenhaRequest;
import com.leilao.userprofile.dto.AtualizarPerfilRequest;
import com.leilao.userprofile.dto.PerfilResponse;
import com.leilao.userprofile.exception.BusinessException;
import com.leilao.userprofile.repository.UsuarioRepository;
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
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Apenas imagens são permitidas para o avatar");
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
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(nomeArquivo);
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
            Path destino = dir.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar avatar: " + e.getMessage());
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
