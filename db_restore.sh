cat backup.sql | docker-compose exec -T mysql sh -c 'mysql -uroot  vrchat' && echo  恢复backup.sql成功
