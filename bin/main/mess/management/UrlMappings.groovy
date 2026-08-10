package mess.management

class UrlMappings {

    static mappings = {
        group "/api", {

            // --- Custom routes (declared BEFORE the generic CRUD catch-alls) ---

            // Dashboard
            get "/dashboard/$monthId"(controller: 'dashboard', action: 'show')

            // Reports
            get "/reports/monthly/$monthId"(controller: 'report', action: 'monthly')
            get "/reports/daily/$monthId/$date"(controller: 'report', action: 'daily')
            get "/reports/member/$memberId/$monthId"(controller: 'report', action: 'member')

            // Members
            put "/members/$id/toggleActive"(controller: 'member', action: 'toggleActive')

            // Months
            put "/months/$id/close"(controller: 'month', action: 'close')
            put "/months/$id/reopen"(controller: 'month', action: 'reopen')
            put "/months/$id/manager/$memberId"(controller: 'month', action: 'setManager')

            // Meals
            post "/meals/generate/$monthId"(controller: 'meal', action: 'generate')
            get "/meals/byDate/$monthId/$date"(controller: 'meal', action: 'byDate')
            get "/meals/byMonth/$monthId"(controller: 'meal', action: 'byMonth')
            put "/meals/$id/toggle"(controller: 'meal', action: 'toggle')

            // Per-month collections
            get "/bazar/byMonth/$monthId"(controller: 'bazar', action: 'byMonth')
            get "/expenses/byMonth/$monthId"(controller: 'expense', action: 'byMonth')
            get "/deposits/byMonth/$monthId"(controller: 'deposit', action: 'byMonth')
            get "/rents/byMonth/$monthId"(controller: 'rent', action: 'byMonth')

            // --- Generic REST CRUD by convention (declared LAST) ---
            delete "/$controller/$id(.$format)?"(action: "delete")
            get "/$controller(.$format)?"(action: "index")
            get "/$controller/$id(.$format)?"(action: "show")
            post "/$controller(.$format)?"(action: "save")
            put "/$controller/$id(.$format)?"(action: "update")
            patch "/$controller/$id(.$format)?"(action: "patch")
        }

        "/"(controller: 'application', action: 'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
