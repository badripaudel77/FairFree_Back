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
