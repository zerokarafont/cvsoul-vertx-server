## 数据库配置
`docker run --name mongo -e MONGO_INITDB_ROOT_USERNAME=mongoadmin -e MONGO_INITDB_ROOT_PASSWORD=xxxxxx -p 3306:27017 -d mongo --wiredTigerCacheSizeGB 1.5`

### 创建数据库用户
`use admin`
`db.auth('rootUsername','rootPasswd')`
`use appdb`
`db.createUser({user:'xxx',pwd:'xxx',roles:[{role:'dbOwner', db:'appdb'}]})`

## 接口约定

## 事务
- 此客户端不支持事务, 需要手动控制
  + [two-phase-commit](https://www.codementor.io/@christkv/mongodb-transactions-vs-two-phase-commit-u6blq7465)
  + [perform two phase commits](https://www.docs4dev.com/docs/en/mongodb/v3.6/reference/tutorial-perform-two-phase-commits.html)

## 数据模型
### voice 音频
```yaml
{
  "_id": String
  "isOpen": Boolean # 是否开放
  "cv": {
       "name": String # 声优姓名
  },
  "url": String # 网络链接
  "text": String # 文本内容
  "pronounce": String # 发音
  "translate": String # 翻译
}
```
### quote_album 语录集
```yaml
{
  "_id": String
  "userId": String # 创建者
  "isOpen": Boolean # 是否公开
  "cover": String # 封面
  "title": String # 标题
  "desc": String # 描述
  "tags": Array<String> # 标签
}
```
### album_voice 语录-音频-多对多
```yaml
{
  "_id": String
  "voiceId": String
  "albumId": String
}
```
