package mess.management

/**
 * Read-only money log: merges bazar, expense, deposit and rent entries for a
 * month into a single timestamped feed, newest first. Accessible to everyone
 * (GET is allowed for all authenticated roles).
 */
class LedgerController {

    static responseFormats = ['json']

    def index() {
        Long monthId = params.long('monthId')
        Month month = Month.get(monthId)
        if (!month) {
            render status: 404, text: '{"message":"Month not found"}'
            return
        }

        List rows = []

        Bazar.findAllByMonth(month).each { Bazar b ->
            rows << [
                    type: 'bazar', id: b.id,
                    memberId: b.member?.id, memberName: b.member?.name,
                    amount: b.amount, description: b.description,
                    entryDate: b.bazarDate?.toString(),
                    createdAt: b.dateCreated?.toInstant()?.toString()
            ]
        }

        Expense.findAllByMonth(month).each { Expense e ->
            rows << [
                    type: 'expense', id: e.id,
                    memberId: e.paidBy?.id, memberName: e.paidBy?.name,
                    amount: e.amount, description: e.description,
                    category: e.category,
                    entryDate: e.expenseDate?.toString(),
                    createdAt: e.dateCreated?.toInstant()?.toString()
            ]
        }

        Deposit.findAllByMonth(month).each { Deposit d ->
            rows << [
                    type: 'deposit', id: d.id,
                    memberId: d.member?.id, memberName: d.member?.name,
                    amount: d.amount, description: d.description,
                    entryDate: d.depositDate?.toString(),
                    createdAt: d.dateCreated?.toInstant()?.toString()
            ]
        }

        Rent.findAllByMonth(month).each { Rent r ->
            rows << [
                    type: 'rent', id: r.id,
                    memberId: r.member?.id, memberName: r.member?.name,
                    amount: r.amount, description: null,
                    entryDate: null,
                    createdAt: r.dateCreated?.toInstant()?.toString()
            ]
        }

        rows.sort { a, b -> (b.createdAt ?: '') <=> (a.createdAt ?: '') }
        respond rows
    }
}
