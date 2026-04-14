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

## Github actions
```yaml
    steps:
      - uses: actions/checkout@v4
        with:
          # Добавить эти опции
          submodules: recursive
          token: ${{ secrets.PAT_TOKEN }}
```

## PAT_TOKEN
Чтоб PAT_TOKEN появился, нужно перейти в:

You accound -> Settings -> Develop Settings -> Personal Access Tokens -> Tokens (classic).

Здесь нужно добавить токен с доступом к репозиторию, actions и workflow.

При сохранении токена нужно скопировать токен и сохранить его в Project -> Settings -> Secrets -> Actions
в переменной PAT_TOKEN
