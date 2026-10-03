pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'M2_HOME'
        nodejs 'NodeJS20'
    }

    environment {
        
        SONAR_TOKEN     = credentials('sonar-token')
        DOCKER_CREDS    = credentials('dockerhub-credentials')
        NEXUS_CREDS     = credentials('nexus-credentials')

        
        IMAGE_NAME      = "mariemjls-5arctict7-appgestionprojets"
        IMAGE_TAG       = "${BUILD_NUMBER}"

        
        SONAR_HOST      = "http://sonarqube:9000"

        
        NEXUS_URL       = "http://192.168.33.10:8081"
    }

    stages {

        // ==========================================
        // STAGE 1 : Checkout SCM
        // ==========================================
        stage('1. Checkout SCM') {
            steps {
                echo "=== Stage 1 : Récupération du code depuis GitHub ==="
                git branch: 'main',
                    credentialsId: 'github-credentials',
                    url: 'https://github.com/mariem-jls/5ArcTic7-Mariem.git'
            }
        }

        // ==========================================
        // STAGE 2 : Maven Compile
        // ==========================================
        stage('2. Maven Compile') {
            steps {
                echo "=== Stage 2 : Compilation Maven ==="
                dir('backend') {
                    sh 'mvn clean compile'
                }
            }
        }

        // ==========================================
        // STAGE 3 : SonarQube Analysis
        // ==========================================
        stage('3. SonarQube Analysis') {
            steps {
                echo "=== Stage 3 : Analyse qualité du code ==="
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=devops-appgestionprojets \
                                -Dsonar.host.url=${SONAR_HOST} \
                                -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        // ==========================================
        // STAGE 4 : Maven Test (3+ méthodes)
        // ==========================================
        stage('4. Maven Test') {
            steps {
                echo "=== Stage 4 : Exécution des tests unitaires ==="
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ==========================================
        // STAGE 5 : Maven Package (.jar)
        // ==========================================
        stage('5. Maven Package') {
            steps {
                echo "=== Stage 5 : Création du livrable .jar ==="
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
                }
            }
        }

        // ==========================================
        // STAGE 6 : Maven Deploy to Nexus
        // ==========================================
        stage('6. Maven Deploy to Nexus') {
            steps {
                echo "=== Stage 6 : Déploiement vers Nexus ==="
                dir('backend') {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh """
                            mvn deploy -DskipTests \
                                -Dnexus.username=${NEXUS_USER} \
                                -Dnexus.password=${NEXUS_PASS}
                        """
                    }
                }
            }
        }

        // ==========================================
        // STAGE 7 : Docker Build & Push
        // ==========================================
        stage('7. Docker Build & Push') {
            steps {
                echo "=== Stage 7 : Construction et push de l'image Docker ==="
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        docker build -t \$DOCKER_USER/${IMAGE_NAME}:${IMAGE_TAG} ./backend
                        docker tag \$DOCKER_USER/${IMAGE_NAME}:${IMAGE_TAG} \$DOCKER_USER/${IMAGE_NAME}:latest
                        docker push \$DOCKER_USER/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push \$DOCKER_USER/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        // ==========================================
        // STAGE 8 : Docker Compose Up
        // ==========================================
        stage('8. Docker Compose Up') {
            steps {
                echo "=== Stage 8 : Déploiement avec Docker Compose ==="
                sh '''
                    docker-compose down || true
                    docker-compose up -d
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminé avec succès !'
        }
        failure {
            echo 'Pipeline échoué. Vérifie les logs.'
        }
        always {
            echo "Build #${BUILD_NUMBER} terminé à ${new Date()}"
        }
    }
}
