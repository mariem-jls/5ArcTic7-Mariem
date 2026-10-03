pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'M2_HOME'
        nodejs 'NodeJS20'
    }

    environment {
        // ========== CREDENTIALS ==========
        SONAR_TOKEN       = credentials('sonar-token')
        DOCKER_CREDS      = credentials('dockerhub-credentials')
        NEXUS_CREDS       = credentials('nexus-credentials')

        // ========== IMAGES DOCKER ==========
        // Format exigé : nomprenom_classe_nomProjet
        BACKEND_IMAGE     = "mariemjls-5arctict7-appgestionprojets-backend"
        FRONTEND_IMAGE    = "mariemjls-5arctict7-appgestionprojets-frontend"
        IMAGE_TAG         = "${BUILD_NUMBER}"

        // ========== URLS SERVICES ==========
        SONAR_HOST        = "http://192.168.33.10:9000"
        NEXUS_URL         = "http://192.168.33.10:8081"
    }

    stages {

        // ==========================================
        // STAGE 1 : Get Code from Git
        // ==========================================
        stage('1. Get Code from Git') {
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
                echo "=== Stage 2 : Compilation Maven (backend) ==="
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
                echo "=== Stage 3 : Analyse qualité du code avec SonarQube ==="
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=devops-appgestionprojets \
                                -Dsonar.projectName="DevOps AppGestionProjets" \
                                -Dsonar.host.url=${SONAR_HOST} \
                                -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        // ==========================================
        // STAGE 4 : Maven Test (3 méthodes minimum)
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
        // STAGE 6 : Maven Deploy -DskipTests
        // ==========================================
        stage('6. Maven Deploy (Skip Tests)') {
            steps {
                echo "=== Stage 6 : Déploiement du .jar vers Nexus ==="
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
        // STAGE 7 : Build & Push Backend Docker
        // ==========================================
        stage('7. Docker Build & Push Backend') {
            steps {
                echo "=== Stage 7 : Build & Push image Docker du backend ==="
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        
                        # Build image backend
                        docker build -t \$DOCKER_USER/${BACKEND_IMAGE}:${IMAGE_TAG} ./backend
                        docker tag \$DOCKER_USER/${BACKEND_IMAGE}:${IMAGE_TAG} \$DOCKER_USER/${BACKEND_IMAGE}:latest
                        
                        # Push image backend
                        docker push \$DOCKER_USER/${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push \$DOCKER_USER/${BACKEND_IMAGE}:latest
                        
                        # Logout
                        docker logout
                    """
                }
            }
        }

        // ==========================================
        // STAGE 8 : Build & Push Frontend Docker (Angular)
        // ==========================================
        stage('8. Docker Build & Push Frontend (Angular)') {
            steps {
                echo "=== Stage 8 : Build & Push image Docker du frontend Angular ==="
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        
                        # Build image frontend Angular
                        docker build -t \$DOCKER_USER/${FRONTEND_IMAGE}:${IMAGE_TAG} ./frontend
                        docker tag \$DOCKER_USER/${FRONTEND_IMAGE}:${IMAGE_TAG} \$DOCKER_USER/${FRONTEND_IMAGE}:latest
                        
                        # Push image frontend
                        docker push \$DOCKER_USER/${FRONTEND_IMAGE}:${IMAGE_TAG}
                        docker push \$DOCKER_USER/${FRONTEND_IMAGE}:latest
                        
                        # Logout
                        docker logout
                    """
                }
            }
        }

        // ==========================================
        // STAGE 9 : Docker Compose Up (Livraison Continue)
        // ==========================================
        stage('9. Docker Compose Up') {
            steps {
                echo "=== Stage 9 : Déploiement avec Docker Compose ==="
                sh '''
                    # Arrêter et nettoyer les conteneurs existants
                    docker stop spring-backend 2>/dev/null || true
                    docker stop mysql-db 2>/dev/null || true
                    docker rm spring-backend 2>/dev/null || true
                    docker rm mysql-db 2>/dev/null || true
                    
                    # Lancer docker-compose
                    docker-compose down || true
                    docker-compose up -d
                    
                    # Attendre que les services démarrent
                    sleep 15
                    
                    # Vérifier que les conteneurs tournent
                    docker ps
                '''
            }
        }
    }

    post {
        success {
            echo '''
            ================================================
            Pipeline terminé avec succès !
            ================================================
            - Backend  : ${BACKEND_IMAGE}:${IMAGE_TAG}
            - Frontend : ${FRONTEND_IMAGE}:${IMAGE_TAG}
            - Nexus    : ${NEXUS_URL}
            - Sonar    : ${SONAR_HOST}
            ================================================
            '''
        }
        failure {
            echo '''
            ================================================
            Pipeline échoué. Vérifie les logs ci-dessus.
            ================================================
            '''
        }
        always {
            echo "Build #${BUILD_NUMBER} terminé le ${new Date()}"
        }
    }
}
