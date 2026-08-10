package mess.management

import grails.util.Environment
import java.time.LocalDate

class BootStrap {

    MealService mealService
    JwtService jwtService

    def init = { servletContext ->
        if (Environment.current == Environment.DEVELOPMENT) {
            seedDevData()
        }
    }

    def destroy = {
    }

    private void seedDevData() {
        // Users — find-or-create explicitly (findOrCreateBy* ignores trailing closures,
        // so non-queried fields like password would stay null → validation failure)
        String adminPass = jwtService.hashPassword('admin123')
        String userPass = jwtService.hashPassword('pass123')

        User admin   = User.findByUsername('admin')
        if (admin) { if (!admin.password.startsWith('$2a$')) { admin.password = adminPass; admin.save(flush:true) } }
        else { admin = new User(username: 'admin',  password: adminPass, role: 'ADMIN').save(failOnError: true) }
        
        User member1 = User.findByUsername('rahman')
        if (member1) { if (!member1.password.startsWith('$2a$')) { member1.password = userPass; member1.save(flush:true) } }
        else { member1 = new User(username: 'rahman', password: userPass,  role: 'MEMBER').save(failOnError: true) }

        User member2 = User.findByUsername('karim')
        if (member2) { if (!member2.password.startsWith('$2a$')) { member2.password = userPass; member2.save(flush:true) } }
        else { member2 = new User(username: 'karim',  password: userPass,  role: 'MEMBER').save(failOnError: true) }

        User member3 = User.findByUsername('salim')
        if (member3) { if (!member3.password.startsWith('$2a$')) { member3.password = userPass; member3.save(flush:true) } }
        else { member3 = new User(username: 'salim',  password: userPass,  role: 'MEMBER').save(failOnError: true) }

        // Members
        Member m1 = Member.findByName('Rahman') ?: new Member(name: 'Rahman', phone: '+8801712345678', joinDate: LocalDate.of(2026,1,1), user: member1).save(failOnError: true)
        Member m2 = Member.findByName('Karim')  ?: new Member(name: 'Karim',  phone: '+8801812345678', joinDate: LocalDate.of(2026,1,1), user: member2).save(failOnError: true)
        Member m3 = Member.findByName('Salim')  ?: new Member(name: 'Salim',  phone: '+8801912345678', joinDate: LocalDate.of(2026,1,1), user: member3).save(failOnError: true)
        Member m4 = Member.findByName('Faruk')  ?: new Member(name: 'Faruk',  phone: '+8801612345678', joinDate: LocalDate.of(2026,1,1), user: null).save(failOnError: true)

        // Month — August 2026 (current open month)
        Month aug = Month.findByYearAndMonthNo(2026, 8)
        if (!aug) {
            aug = new Month(year: 2026, monthNo: 8, manager: m1)
            aug.save(failOnError: true)
        }

        // Rent — 1500 per member for August
        [m1, m2, m3, m4].each { Member m ->
            Rent existing = Rent.where { member == m && month == aug }.get()
            if (!existing) {
                new Rent(member: m, month: aug, amount: 1500.0G).save(failOnError: true)
            }
        }

        // Bazar entries
        List bazarData = [
                [member: m1, amount: 2200, description: 'Rice, oil, spices', date: LocalDate.of(2026, 8, 1)],
                [member: m2, amount: 1800, description: 'Vegetables, fish',  date: LocalDate.of(2026, 8, 5)],
                [member: m1, amount: 1500, description: 'Lentils, onions',   date: LocalDate.of(2026, 8, 10)],
                [member: m3, amount: 1000, description: 'Chicken, eggs',     date: LocalDate.of(2026, 8, 15)],
        ]
        bazarData.each { d ->
            if (!Bazar.findByMemberAndBazarDate(d.member as Member, d.date as LocalDate)) {
                new Bazar(member: d.member, month: aug, amount: d.amount as BigDecimal,
                        description: d.description as String, bazarDate: d.date as LocalDate).save(failOnError: true)
            }
        }

        // Expenses
        List expenseData = [
                [month: aug, amount: 2500, description: 'Gas bill',     category: 'gas',         date: LocalDate.of(2026, 8, 5),  paidBy: m1],
                [month: aug, amount: 1200, description: 'Internet',     category: 'internet',    date: LocalDate.of(2026, 8, 10), paidBy: m2],
                [month: aug, amount: 800,  description: 'Water bill',   category: 'water',       date: LocalDate.of(2026, 8, 12), paidBy: m1],
                [month: aug, amount: 500,  description: 'Other stuff',  category: 'other',       date: LocalDate.of(2026, 8, 15), paidBy: m3],
        ]
        expenseData.each { d ->
            // Guard on month + date + category so dev reboots don't re-seed duplicates.
            if (!Expense.findByMonthAndExpenseDateAndCategory(d.month as Month, d.date as LocalDate, d.category as String)) {
                new Expense(month: d.month as Month, amount: d.amount as BigDecimal,
                        description: d.description as String, category: d.category as String,
                        expenseDate: d.date as LocalDate, paidBy: d.paidBy as Member).save(failOnError: true)
            }
        }

        // Deposits
        List depositData = [
                [member: m1, amount: 3000, description: 'August deposit', date: LocalDate.of(2026, 8, 1)],
                [member: m2, amount: 3000, description: 'August deposit', date: LocalDate.of(2026, 8, 2)],
                [member: m3, amount: 3000, description: 'August deposit', date: LocalDate.of(2026, 8, 2)],
                [member: m4, amount: 3000, description: 'August deposit', date: LocalDate.of(2026, 8, 3)],
        ]
        depositData.each { d ->
            // Guard on member + date so dev reboots don't re-seed duplicates.
            if (!Deposit.findByMemberAndDepositDate(d.member as Member, d.date as LocalDate)) {
                new Deposit(member: d.member, month: aug, amount: d.amount as BigDecimal,
                        description: d.description as String, depositDate: d.date as LocalDate).save(failOnError: true)
            }
        }

        // Generate meals for all active members × every day of August
        // (idempotent — skips existing rows)
        mealService.generateDefaultMeals(aug.id)

        // Toggle some meals OFF for a bit of variety
        LocalDate aug5 = LocalDate.of(2026, 8, 5)
        mealService.toggleFor(m2.id, aug5, 'dinner', false)
        mealService.toggleFor(m3.id, aug5, 'breakfast', false)
        LocalDate aug10 = LocalDate.of(2026, 8, 10)
        mealService.toggleFor(m1.id, aug10, 'dinner', false)
    }
}
