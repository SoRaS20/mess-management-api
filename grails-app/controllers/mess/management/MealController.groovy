package mess.management

import grails.rest.RestfulController
import java.time.LocalDate
import java.time.format.DateTimeParseException

class MealController extends RestfulController<Meal> {

    static responseFormats = ['json']

    MealService mealService

    MealController() {
        super(Meal)
    }

    def byDate() {
        LocalDate date = parseDate(params.date)
        if (!date) {
            render status: 400, text: 'Invalid or missing date (expected yyyy-MM-dd)'
            return
        }
        respond mealService.byDate(params.long('monthId'), date)
    }

    def byMonth() {
        respond mealService.byMonth(params.long('monthId'))
    }

    def toggle() {
        def body = request.JSON
        String slot = body.slot
        if (!slot) {
            render status: 400, text: 'Missing "slot" (breakfast|lunch|dinner)'
            return
        }
        try {
            Meal meal = mealService.toggle(params.long('id'), slot, body.on as boolean)
            if (!meal) {
                render status: 404
                return
            }
            respond meal
        } catch (IllegalArgumentException e) {
            render status: 400, text: e.message
        }
    }

    def generate() {
        Long monthId = params.long('monthId')
        if (monthId == null) {
            render status: 400, text: 'Missing monthId'
            return
        }
        respond([created: mealService.generateDefaultMeals(monthId)])
    }

    private static LocalDate parseDate(String value) {
        if (!value) return null
        try {
            LocalDate.parse(value)
        } catch (DateTimeParseException ignored) {
            null
        }
    }
}
