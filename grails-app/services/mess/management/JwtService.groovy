package mess.management

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.SecretKey
import java.util.Date

class JwtService {

    // Very long secret for HMAC-SHA256 (in real apps, use environment variables)
    private static final String SECRET = "mess_management_super_secret_jwt_key_that_is_long_enough_for_hmac_sha256"
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes("UTF-8"))

    String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt())
    }

    boolean checkPassword(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed)
    }

    String generateToken(User user, Long memberId) {
        // Expiration: 7 days
        Date now = new Date()
        Date exp = new Date(now.time + 1000L * 60 * 60 * 24 * 7)

        return Jwts.builder()
                .subject(user.username)
                .claim("userId", user.id)
                .claim("role", user.role)
                .claim("memberId", memberId)
                .issuedAt(now)
                .expiration(exp)
                .signWith(KEY)
                .compact()
    }

    Map validateToken(String token) {
        try {
            def claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            return [
                    userId: claims.get("userId", Long.class),
                    role: claims.get("role", String.class),
                    memberId: claims.get("memberId", Long.class),
                    username: claims.subject
            ]
        } catch (Exception e) {
            log.warn("Invalid JWT: ${e.message}")
            return null
        }
    }
}
