package mess.management

import grails.rest.RestfulController
import grails.gorm.transactions.Transactional
import java.time.LocalDate

class MemberController extends RestfulController<Member> {

    static responseFormats = ['json']

    MemberService memberService
    JwtService jwtService

    MemberController() {
        super(Member)
    }

    @Transactional
    @Override
    def save() {
        def json = request.JSON
        
        Member member = new Member(
            name: json.name, 
            phone: json.phone, 
            joinDate: json.joinDate ? LocalDate.parse(json.joinDate as String) : LocalDate.now()
        )
        
        if (json.createAppUser && json.username && json.password) {
            if (User.findByUsername(json.username as String)) {
                member.errors.rejectValue('user', 'unique', 'Username is already taken')
                respond member.errors, status: 422
                return
            }
            
            String hash = jwtService.hashPassword(json.password as String)
            User user = new User(username: json.username, password: hash, role: 'MEMBER').save(failOnError: true)
            member.user = user
        }
        
        member.validate()
        if (member.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond member.errors, status: 422
            return
        }
        
        member.save(flush: true)
        respond member, status: 201
    }

    def toggleActive() {
        Member member = memberService.toggleActive(params.long('id'))
        if (!member) {
            render status: 404
            return
        }
        respond member
    }
}
