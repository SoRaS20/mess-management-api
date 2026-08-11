package mess.management

class BootStrap {

    JwtService jwtService

    def init = { servletContext ->
        seedAdmin()
    }

    def destroy = {
    }

    private Map<String, String> loadDotEnv() {
        Map<String, String> env = [:]
        File file = new File('.env')
        if (file.exists()) {
            file.eachLine { line ->
                if (line.trim() && !line.startsWith('#')) {
                    def parts = line.split('=', 2)
                    if (parts.length == 2) {
                        env[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }
        return env
    }

    private void seedAdmin() {
        try {
            def dotEnv = loadDotEnv()
            String adminUsername = dotEnv.ADMIN_USERNAME ?: System.getenv('ADMIN_USERNAME') ?: 'admin'
            String adminPassPlain = dotEnv.ADMIN_PASSWORD ?: System.getenv('ADMIN_PASSWORD') ?: 'admin123'

            User.withTransaction { status ->
                String adminPass = jwtService.hashPassword(adminPassPlain)

                User admin = User.findByUsername(adminUsername)
                if (admin) {
                    if (!admin.password.startsWith('$2a$')) {
                        admin.password = adminPass
                        admin.save(flush: true, failOnError: true)
                    }
                } else {
                    new User(username: adminUsername, password: adminPass, role: 'ADMIN').save(flush: true, failOnError: true)
                }
            }
        } catch (Exception e) {
            println "Error during seedAdmin: ${e.message}"
            e.printStackTrace()
        }
    }
}
