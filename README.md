## Geo-service

#### Технологии, сборка и запуск

Основной стек: 
[Scala](https://www.scala-lang.org/), [sbt](http://www.scala-sbt.org/), [Akka-Http, Akka Stream, Akka Persistence](https://doc.akka.io/docs/akka-http/current/?language=scala)

Собрать приложение: `sbt assembly`

Output path: `assembly/geo-service-0.1.jar`

Пример запуска приложения:
`java -jar ./target/scala-2.12/geo-service-0.1.jar -g ./src/main/resources/grid.txt -u ./src/main/resources/user_labels.txt`

Описание параметров запуска:

- -u, --user-storage - путь до файла с пользовательскими метками
- -g, --grid-storage  - geo сетка

Описание api:

- Добавление метки:

    Post: `"/marker/insert"` body: `{userId: Long, lon: Float, lat: Float}`
    response: `{userId: Long}`
    Сложность выполнения запроса: O(1)
- Обновление метки:
    
    Put: `"/marker/update"` body: `{userId: Long, lon: Float, lat: Float}`
    response: `{userId: Long}`
    Сложность выполнения запроса: O(1)

- Удаление метки:
    
    Delete: `"/marker/delete"` body: `{userId: Long, lon: Float, lat: Float}`
    response: `{userId: Long}`
    Сложность выполнения запроса: O(1)
    
- Получить информацию о место положения:
    
    Get: `"/find/marker?userId=1&lon=1.23&lan=2.323"` 
    response: `String`
    Сложность выполнения запроса: O(1)
    
- Получить статистику по метке:
    
    Get: `"/point/info?lon=2&lan=3"` 
    response: `{countUser: Int}`
    Сложность выполнения запроса: O(1)

Данные из файлов загружаются RAM. 
Если есть ограничение по памяти то возможно стоит посмотреть в сторону 
off-heap подхода.
В качестве storage используется thread-safe TrieMap, алгоритм lock-free.
При каждой операции с метками пользователя обновляем статистику сетки.
В качестве сохранения состояния после сбоев или остановки используется 
akka persistence.

Для определения расстояния между точками использовалась 
формула модификация для антиподов

#### Генерация таблиц

Генерация реализована на python, стандартными средствами.
`scripts/generate_data_files.py --min_lon -40 --max_lon 40 --min_lan -60 --max_lan 60`

#### Замечания и дальнейшие доработки

1. Хранить данные в NoSQL или RDBMS базе данных.
2. Микросервисную архитектуру (разбить логику на сервисы)
3. Сделать систему более масштабируемой, как пример Akka Cluster
