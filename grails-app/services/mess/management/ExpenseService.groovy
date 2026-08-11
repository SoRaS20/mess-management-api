package mess.management

import grails.gorm.transactions.Transactional

@Transactional
class ExpenseService {

    Expense get(Long id) {
        Expense.get(id)
    }

    List<Expense> byMonth(Long monthId) {
        Month month = Month.get(monthId)
        month ? Expense.findAllByMonth(month, [sort: 'expenseDate']) : []
    }

    Expense create(Map args) {
        Expense expense = new Expense(args)
        expense.save(failOnError: true)
        expense
    }

    Expense update(Long id, Map args) {
        Expense expense = Expense.get(id)
        if (!expense) return null
        expense.properties = args
        expense.save(failOnError: true)
        expense
    }

    boolean delete(Long id) {
        Expense expense = Expense.get(id)
        if (!expense) return false
        expense.delete(flush: true)
        true
    }

    @Transactional(readOnly = true)
    BigDecimal totalExpenses(Long monthId) {
        byMonth(monthId).inject(0.0G) { BigDecimal sum, Expense e -> sum + (e.amount ?: 0.0G) }
    }

    @Transactional(readOnly = true)
    BigDecimal totalExpenseFor(Long memberId, Long monthId) {
        Member theMember = Member.get(memberId)
        Month theMonth = Month.get(monthId)
        if (!theMember || !theMonth) return 0.0G
        Expense.where { paidBy == theMember && month == theMonth }.list()
                .inject(0.0G) { BigDecimal sum, Expense e -> sum + (e.amount ?: 0.0G) }
    }
}
