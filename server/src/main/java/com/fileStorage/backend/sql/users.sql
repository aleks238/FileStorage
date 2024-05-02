create table users(
    id          int not null auto_increment,
    login       varchar(40) not null,
    password    varchar(40) not null,
    email       varchar(80),
    primary key (id)
);





