package mess.management

class AuthInterceptor {

    JwtService jwtService

    AuthInterceptor() {
        match(uri: "/api/**").excludes(uri: "/api/login")
    }

    boolean before() {
        if (request.method == 'OPTIONS') return true // Allow CORS preflight if any

        String authHeader = request.getHeader('Authorization')
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            render status: 401, text: 'Unauthorized: No token provided'
            return false
        }

        String token = authHeader.substring(7)
        def claims = jwtService.validateToken(token)

        if (!claims) {
            render status: 401, text: 'Unauthorized: Invalid or expired token'
            return false
        }

        // Attach claims to request attributes so controllers can use them
        request.userId = claims.userId
        request.role = claims.role
        request.memberId = claims.memberId
        request.username = claims.username

        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
