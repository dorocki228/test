package l2s.commons.math.random;

import l2s.commons.util.Rnd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class RndSelector<E> {
    private static final RndSelector EMPTY_RND_SELECTOR = new EmptyRndSelector();

    public static <T> RndSelector<T> create(Collection<RndNode<T>> rndNodes) {
        RndNode<T>[] array = rndNodes.toArray(new RndNode[0]);
        return create(array);
    }

    @SafeVarargs
    public static <T> RndSelector<T> create(RndNode<T>... rndNodes) {
        if (rndNodes == null || rndNodes.length == 0) {
            return EMPTY_RND_SELECTOR;
        }

        if (rndNodes.length == 1) {
            return new SingleRndSelector<>(rndNodes[0]);
        }

        Arrays.sort(rndNodes);
        return new MultipleRndSelector<>(rndNodes);
    }

    @SafeVarargs
    public static <T> RndSelector<T> createAndClean(RndNode<T>... rndNodes) {
        RndNode<T>[] cleanedArray = Arrays.stream(rndNodes)
                .filter(Objects::nonNull)
                .toArray(RndNode[]::new);
        return create(cleanedArray);
    }

    /**
     * Вернет один из елементов или null, null возможен только если сумма весов всех элементов меньше maxWeight
     */
    public abstract E chance(int maxWeight);

    /**
     * Вернет один из елементов
     */
    public abstract E select();

    private static final class EmptyRndSelector<E> extends RndSelector<E> {
        @Override
        public E chance(int maxWeight) {
            return null;
        }

        @Override
        public E select() {
            return null;
        }
    }

    private static final class SingleRndSelector<E> extends RndSelector<E> {
        private final RndNode<E> node;

        SingleRndSelector(RndNode<E> node) {
            this.node = node;
        }

        @Override
        public E chance(int maxWeight) {
            if (maxWeight <= 0) {
                return null;
            }

            if (node.getWeight() > Rnd.get(maxWeight)) {
                return node.getValue();
            }

            return null;
        }

        @Override
        public E select() {
            return chance(node.getWeight());
        }
    }

    private static final class MultipleRndSelector<E> extends RndSelector<E> {
        private final RndNode<E>[] nodes;

        MultipleRndSelector(RndNode<E>[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public E chance(int maxWeight) {
            if (maxWeight <= 0)
                return null;

            int r = Rnd.get(maxWeight);
            int weight = 0;
            for (RndNode<E> node : nodes) {
                weight += node.getWeight();
                if (weight > r)
                    return node.getValue();
            }

            return null;
        }

        @Override
        public E select() {
            int totalWeight = Stream.of(nodes).mapToInt(RndNode::getWeight).sum();
            return chance(totalWeight);
        }
    }

    public static class RndNode<T> implements Comparable<RndNode<T>> {
        private final T value;
        private final int weight;

        private RndNode(T value, int weight) {
            this.value = value;
            this.weight = weight;
        }

        public static <T> RndNode<T> create(T value, int weight) {
            return new RndNode<>(value, weight);
        }

        public T getValue() {
            return value;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public int compareTo(RndNode<T> o) {
            return Integer.compare(weight, o.getWeight());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            RndNode<?> rndNode = (RndNode<?>) o;
            return weight == rndNode.weight &&
                    Objects.equals(value, rndNode.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, weight);
        }
    }
}