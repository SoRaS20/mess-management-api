package mess.management

import grails.core.GrailsApplication
import grails.plugins.*
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager
    DataSource dataSource

    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

    def health() {
        render status: 200, text: 'ok'
    }

    def ready() {
        Connection connection = null
        Statement statement = null

        try {
            connection = dataSource.connection
            statement = connection.createStatement()
            statement.execute('SELECT 1')
            render status: 200, text: 'ready'
        } catch (Exception e) {
            log.warn('Database readiness check failed', e)
            render status: 503, text: 'database unavailable'
        } finally {
            statement?.close()
            connection?.close()
        }
    }
}
