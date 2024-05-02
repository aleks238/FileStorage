## Краткое содержание:
Проект файлового хранилища, упрощенная копия Google Диск. 

### Backend:
* Java version: 8
* Netty framework
* MySQL

### Frontend:
* JavaFX
* Netty framework

### Сборка и запуск:
* Установить сервер MySQL, создать базу данных: file_storage_database,  создать таблицу users. Скрипт для таблицы можно взять из файла sql: backend/sql/users.sql.
* Бэкап будет добавлен позже...
* Запустить сервер (файл ServerNetty). При запуске сервера создается папка users, которая выступает корнем хранилища файлов пользователей.
* Frontend запускается файлом ClientApp.
* Зарегистрировать аккаунт. При успешной регистрации аккаунта на сервере, в корневой директории хранилища "users" создастся директория хранилища пользователя, название которой соответствует логину пользователя.
* Войти в приложение используя свои данные регистрации.
