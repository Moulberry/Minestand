package net.gauntletmc.command.consumers;

public interface NConsumer<A, B, C, D, E, F, G> {

    void accept7(A a, B b, C c, D d, E e, F f, G g);

    void accept6(A a, B b, C c, D d, E e, F f);

    void accept5(A a, B b, C c, D d, E e);

    void accept4(A a, B b, C c, D d);

    void accept3(A a, B b, C c);

    void accept2(A a, B b);

    void accept1(A a);

}
