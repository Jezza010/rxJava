# Реализация реактивных потоков на Java

Этот проект — простая реализация реактивного программирования, вдохновлённая **RxJava**. Всё написано с нуля, чтобы лучше понять, как работают асинхронность и потоки данных.

## Что умеет

* Создавать потоки данных и обрабатывать их с помощью операторов: `map`, `filter`, `flatMap` и др.
* Обрабатывать ошибки и завершение потока
* Управлять выполнением с помощью собственных планировщиков (Executors)

## Основные компоненты

* **Stream<T>** — основной класс для создания и обработки потоков
* **Subscriber<T>** — интерфейс для подписки на события потока
* **Subscription** — позволяет отменить подписку
* **Executor** — абстракция для управления потоками выполнения

## Пример использования

```java
Stream<Integer> stream = Stream.create(subscriber -> {
    for (int i = 0; i < 5; i++) {
        subscriber.onNext(i);
    }
    subscriber.onComplete();
});

stream
    .map(x -> x * 2)
    .filter(x -> x % 3 != 0)
    .subscribe(
        item -> System.out.println("Получено: " + item),
        error -> System.err.println("Ошибка: " + error.getMessage()),
        () -> System.out.println("Готово!")
    );
```

## Планировщики (Executors)

Для управления асинхронностью в проекте реализованы разные типы исполнителей:

* **IOExecutor** — для операций с вводом/выводом
* **ComputeExecutor** — для вычислений
* **SingleExecutor** — для последовательных задач

## Операторы

* `map` — преобразует элементы
* `filter` — отбирает элементы по условию
* `flatMap` — разворачивает вложенные потоки

## Обработка ошибок

Если в потоке возникает ошибка, она передаётся в метод `onError`, где её можно обработать.

## Тесты

В проекте есть модульные тесты, которые проверяют работу операторов, подписок и планировщиков.
