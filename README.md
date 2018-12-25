# SQLKV

xQueryPhrase

token：令牌/标记
tokenize：令牌化
tokenizer：令牌解析器

实现功能点

词法分析器
语法分析器
支持事务
页面分析


MySQL目录结构:https://www.cnblogs.com/lushilin/p/6086833.html

create table t1(id int);

insert into t1 values(1)

select * from t1


MySQL插入数据流程:http://www.aneasystone.com/archives/2018/06/insert-locks-via-mysql-source-code.html


MySQL数据是怎么写到文件的?
FIL_TABLESPACE                表空间space
FIL_LOG                       重做日志space
FIL_ARCHI_LOG                 归档日志space

VTIs (virtual table interface)


CREATE TABLE test ( id INT )
INSERT INTO test ( id ) VALUES ( 1 )
select id from test

SQL语句词法解析Parser
验证数据
