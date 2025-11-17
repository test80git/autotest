pipeline {
    agent any
    environment {
        // Добавляем Java в PATH если нужно
        PATH = "C:\\Program Files\\Java\\jdk-17\\bin;${env.PATH}"
          JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8'
                GRADLE_OPTS = '-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8'
    }

        triggers {
            pollSCM('*/2 * * * *') // Каждые 2 минут
        }

    stages {
        stage('Checkout') {
            steps {
               bat '''
               echo === Checkout ===
               '''
                git 'https://github.com/test80git/autotest.git'
            }
        }

        stage('Diagnostics') {
            steps {
                bat '''
                echo                                    === Project Structure ===
                  dir /B
                  echo                              === Available tasks in monoscript ===
                  call gradlew.bat :monoscript:tasks --no-daemon
                  echo                                       === Test classes ===
                  dir monoscript\\src\\test\\java /S /B
                  echo                                     === Cucumber features ===
                  dir monoscript\\src\\test\\resources\\features /S /B *.feature
                  echo                                   === Encoding Diagnostics ===
                              chcp
                              echo JAVA_HOME: %JAVA_HOME%
                              echo LANG: %LANG%
                              echo Testing Russian text: Тест кириллицы
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
                 echo === Setting UTF-8 Encoding ===
                 chcp 65001
                 echo Testing: Тест кириллицы
                    echo Running tests...
                    call gradlew.bat test --info ^
                    -Dfile.encoding=UTF-8 ^
                    -Dsun.jnu.encoding=UTF-8 ^
                    -Dconsole.encoding=UTF-8 ^
                    -Dstdout.encoding=UTF-8 ^
                    -Dstderr.encoding=UTF-8

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