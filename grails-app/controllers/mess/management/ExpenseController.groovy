package mess.management

import grails.rest.RestfulController

class ExpenseController extends RestfulController<Expense> {

    static responseFormats = ['json']

    ExpenseController() {
        super(Expense)
    }

    def byMonth() {
        respond Expense.findAllByMonth(Month.get(params.long('monthId')), [sort: 'expenseDate'])
    }
}
