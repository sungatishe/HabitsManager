package com.example.habits.repository;

import com.example.habits.domain.RefreshToken;
import com.example.habits.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private RefreshToken refreshToken;


    @BeforeEach
    void SetUp() {
        user = new User();
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test Test");
        userRepository.save(user);

        refreshToken = new RefreshToken();
        refreshToken.setToken("test-refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiryTime(Instant.now().plusSeconds(3600));
        refreshTokenRepository.save(refreshToken);
    }

    @Test
    void shouldFindRefreshTokenByToken() {
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("test-refresh-token");
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo("test-refresh-token");
        assertThat(foundToken.get().getUser().getUsername()).isEqualTo("test");
    }

    @Test
    void shouldNotFindNonExistentRefreshToken() {
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("no-token");
        assertThat(foundToken).isNotPresent();
    }

    @Test
    void shouldDeleteRefreshTokenByToken() {
        refreshTokenRepository.deleteByToken("test-refresh-token");
        Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken("test-refresh-token");
        assertThat(deletedToken).isNotPresent();
    }

    @Test
    void shouldDeleteRefreshTokenByUserId() {
        refreshTokenRepository.deleteByUserId(user.getId());
        Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken("test-refresh-token");
        assertThat(deletedToken).isNotPresent();
    }

    @Test
    void shouldSaveRefreshToken() {
        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-refresh-token");
        newToken.setUser(user);
        newToken.setExpiryTime(Instant.now().plusSeconds(7200));

        RefreshToken savedToken = refreshTokenRepository.save(newToken);

        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("new-refresh-token");
    }

}
