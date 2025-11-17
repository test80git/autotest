pipeline {
    agent any
    environment {
        // Добавляем Java в PATH если нужно
        PATH = "C:\\Program Files\\Java\\jdk-17\\bin;${env.PATH}"
    }
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/test80git/autotest.git'
            }
        }

        stage('Diagnostics') {
            steps {
                bat '''
                echo === Project Structure ===
                  dir /B
                  echo                              === Available tasks in monoscript ===
                  call gradlew.bat :monoscript:tasks --no-daemon
                  echo                                       === Test classes ===
                  dir monoscript\\src\\test\\java /S /B
                  echo                                     === Cucumber features ===
                  dir monoscript\\src\\test\\resources\\features /S /B *.feature
                '''
            }
        }

        stage('Clean') {
            steps {
                bat '''
                    echo Cleaning previous build...
                    call gradlew.bat clean
                '''
            }
        }

        stage('Test') {
            steps {
                bat '''
                    echo Running tests...
                    call gradlew.bat test --info

                '''
            }
        }

        stage('Test Reports') {
            steps {
                bat '''
                    echo Generating test reports...
                    call gradlew.bat test --no-daemon
                '''
                // Публикация результатов JUnit тестов
                junit '**/build/test-results/test/**/*.xml'
            }
        }
    }

    post {
        always {
            echo '=== Test execution completed ==='
            // Публикация Allure отчетов если они есть
            script {
                if (fileExists('build/allure-results')) {
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'build/allure-results']]
                    ])
                }
            }
        }
        success {
            echo '✅ All tests passed successfully!'
        }
        failure {
            echo '❌ Some tests failed!'
        }
    }
}