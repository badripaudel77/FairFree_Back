Please use the following software versions for setting up.
- IntelliJ Idea (Community / Ultimate)
- **Java 25** (We have used the latest version. If you're on mac, use sdkman or homebrew for installation, on windows, point to environment variable or just use the one from intellij IDEA from the project setting)
- **PostgreSQL 17.5** or later. 
- Have **maven**  version **3.9.11** install for build tool.
- Create postgres database called **fairfree**
- API lists (swagger UI) : http://localhost:8080/swagger-ui/index.html

### AWS S3 Policy
- Create AWS S3 Bucket name : **fairfree-item-images**
- Update Policy for Read Object (Read only)
  {
      "Version": "2012-10-17",
      "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::fairfree-item-images/*"
        }
      ]
  }
  In above policy, ```fairfree-item-images``` is the bucket name and it must match.

- Create IAM user for programmatic access, name it : fairfree_app_user
  - Go To IAM Users, policy > new policy and give minimal permission required for this user. Like read, delete, update object and list buckets.
  - Give policy name : FairFree_App_User_Bucket_Policy
  {
      "Version": "2012-10-17",
        "Statement": [
        {
        "Effect": "Allow",
        "Action": ["s3:PutObject","s3:GetObject","s3:DeleteObject"],
        "Resource": "arn:aws:s3:::fairfree-item-images/*"
        },
        {
        "Effect": "Allow",
        "Action": ["s3:ListBucket"],
        "Resource": "arn:aws:s3:::fairfree-item-images"
        }
    ]
  }
  
- Generate Access and Secret Key and download and safely store. Don't put that in code or repo. 
- Pass as env variables while running.
- spring.profiles.active=prod for prod enabling.
If you need to pass the env variables while running through the command, pass it as:
-  mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=prod --AWS_ACCESS_KEY=VALID_ACCESS_KEY --AWS_SECRET_KEY=VALID_SECRET_KEY"

--- For PROD Release ---
1. SPRING_PROFILES_ACTIVE:prod
2. AWS region: us-west-1 (region where s3 bucket lives) - as per the need.
3. If profile is prod, must pass AWS_SECRET_KEY && AWS_ACCESS_KEY.


--- For Observability - Actuator, Prometheus, Grafana ---
1. Actuator exposes endpoints for health data : /actuator/health, /actuator/metrics
2. Prometheus pulls the metrics exposed by  actuator : /actuator/prometheus
3. Grafana : For better visualization

Configure Docker for Prometheus and Grafana. Look for the endpoints to access these.