## 数据库配置
`docker run --name mongo -e MONGO_INITDB_DATABASE=cvsoul -e MONGO_INITDB_ROOT_USERNAME=mongoadmin -e MONGO_INITDB_ROOT_PASSWORD=xxxxxx -p 3306:27017 -d mongo --wiredTigerCacheSizeGB 1.5`

### 创建数据库用户
`use admin`
`db.auth('rootUsername','rootPasswd')`
`use appdb`
`db.createUser({user:'xxx',pwd:'xxx',roles:[{role:'dbOwner', db:'appdb'}]})`

