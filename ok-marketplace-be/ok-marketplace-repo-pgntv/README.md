# Репозиторий на базе PostgreSQL
Native реализация на базе библиотеки pgkn

## PGKN

### Если впервые добавляете подмодуль в git
```bash
git submodule add git@github.com:crowdproj/pgkn.git pgkn
```

### Если подмодуль уже включен в git и нужно его инициализировать
```bash
git submodule update --init --recursive
```

### Обязательно устанавливаем дополнительную библиотеку
```bash
sudo apt install libpq-dev  
```
