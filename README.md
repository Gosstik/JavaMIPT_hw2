Учитывается только последняя версия фукнции, у которой указана аннотация. То есть путь есть наследование C -> B -> A и каждый из этих классов переопределяет один и тот же метод `m`, тогда верно слеедующее:
* `A.m` помечен `@cache`
* 
. Также метод не может быть одновременно помечен как `@Setter` и `@Getter`. В таком случае бросается `RuntimeException`.

TODO:
- [ ] Кэширование статических методов.
- [ ] Кэширование super.

I can't understand how to intercept methods that are called inside class with super.<method>. Could you please give example of how to do this with subclass?

```java


```