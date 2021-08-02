## TODO
- [] 封装返回响应
- [] 封装通用的CRUD分页
- [] 限流

## 数据库配置
`docker run --name mongo -e MONGO_INITDB_ROOT_USERNAME=mongoadmin -e MONGO_INITDB_ROOT_PASSWORD=xxxxxx -p 3306:27017 -d mongo --wiredTigerCacheSizeGB 1.5`

### 创建数据库用户
`use admin`
`db.auth('rootUsername','rootPasswd')`
`use appdb`
`db.createUser({user:'xxx',pwd:'xxx',roles:[{role:'dbOwner', db:'appdb'}]})`

## 登录用户
- 基于角色的认证模式
  + 只有master用户才能访问admin相关接口
## 接口约定

## 事务
- 此客户端不支持事务, 需要手动控制
  + [two-phase-commit](https://www.codementor.io/@christkv/mongodb-transactions-vs-two-phase-commit-u6blq7465)
  + [perform two phase commits](https://www.docs4dev.com/docs/en/mongodb/v3.6/reference/tutorial-perform-two-phase-commits.html)
