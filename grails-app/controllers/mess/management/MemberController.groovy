package mess.management

import grails.rest.RestfulController

class MemberController extends RestfulController<Member> {

    static responseFormats = ['json']

    MemberService memberService

    MemberController() {
        super(Member)
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
