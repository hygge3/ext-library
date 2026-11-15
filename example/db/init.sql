-- 首次准备：建库。表结构与初始数据由应用启动时执行 classpath:db/schema.sql 完成（幂等，可重复启动）。
-- 用法：psql -U postgres -f example/db/init.sql
-- 库名要与 .env 里的 PG_DATABASE 一致。
CREATE DATABASE ext_example;
