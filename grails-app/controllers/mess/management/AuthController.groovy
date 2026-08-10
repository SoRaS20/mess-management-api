package mess.management

import grails.converters.JSON

class AuthController {
    JwtService jwtService

    def login() {
        if (request.method != 'POST') {
            render status: 405
            return
        }

        def req = request.JSON
        if (!req || !req.username || !req.password) {
            render status: 400, text: 'Username and password required'
            return
        }

        User user = User.findByUsername(req.username as String)
        if (!user || !jwtService.checkPassword(req.password as String, user.password)) {
            render status: 401, text: 'Invalid credentials'
            return
        }

        // Find associated member ID (if any)
        Member member = Member.findByUser(user)
        Long memberId = member ? member.id : null

        String token = jwtService.generateToken(user, memberId)

        def responseMap = [
            token: token,
            user: [
                id: user.id,
                username: user.username,
                role: user.role,
                memberId: memberId
            ]
        ]
        
        render responseMap as JSON
    }
}
