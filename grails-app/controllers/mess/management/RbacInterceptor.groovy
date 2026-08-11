package mess.management

class RbacInterceptor {

    int order = 100 // run after AuthInterceptor

    RbacInterceptor() {
        match(uri: "/api/**").excludes(uri: "/api/login")
    }

    boolean before() {
        if (request.method == 'OPTIONS') return true
        if (request.method == 'GET') return true // Read-only access allowed for all authenticated users

        String role = request.role
        if (role == 'ADMIN') return true // Admin can do anything

        // At this point, role is MEMBER
        String c = controllerName
        String a = actionName

        // Admin-only controllers (write actions)
        if (c in ['member', 'month', 'rent', 'deposit', 'report', 'dashboard']) {
            render status: 403, text: 'Forbidden: Admin access required'
            return false
        }

        // Manager controllers (bazar, expense) or Meal generation
        if (c in ['bazar', 'expense'] || (c == 'meal' && a == 'generate')) {
            // Need to check if user is manager of the month
            // Unfortunately, payload/params vary. For simplicity in this MVP,
            // we will fetch the month ID from params or payload and check manager.
            Long monthId = extractMonthId()
            if (!monthId || !isManager(monthId, request.memberId as Long)) {
                render status: 403, text: 'Forbidden: Admin or Manager access required'
                return false
            }
            return true
        }

        // Meal toggle / admin-correction (update) / delete
        if (c == 'meal' && a in ['toggle', 'update', 'delete']) {
            // Check if the meal belongs to the user or the user manages its month
            Long mealId = params.id as Long
            Meal meal = Meal.get(mealId)
            if (!meal) {
                render status: 404, text: 'Meal not found'
                return false
            }
            if (meal.memberId == (request.memberId as Long)) return true
            if (isManager(meal.monthId, request.memberId as Long)) return true
            render status: 403, text: 'Forbidden: You can only toggle your own meals'
            return false
        }

        // Deny anything else by default for MEMBER
        render status: 403, text: 'Forbidden'
        return false
    }

    boolean after() { true }

    void afterView() { }

    private Long extractMonthId() {
        if (params.monthId) return params.monthId as Long
        if (params.id) {
            // Might be updating an existing record, need to look it up
            String c = controllerName
            Long id = params.id as Long
            if (c == 'bazar') return Bazar.get(id)?.monthId
            if (c == 'expense') return Expense.get(id)?.monthId
        }
        if (request.JSON && request.JSON.month) {
            // usually month: { id: X }
            def monthVal = request.JSON.month
            if (monthVal instanceof Map) return monthVal.id as Long
            return monthVal as Long
        }
        return null
    }

    private boolean isManager(Long monthId, Long memberId) {
        if (!memberId) return false
        Month m = Month.get(monthId)
        return m?.managerId == memberId
    }
}
