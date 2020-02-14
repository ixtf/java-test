package org.jzb.lintcode.wordBreak3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author jzb 2020-02-01
 */
public class Solution {

    public static void main(String[] args) {
        final String s = "Catmat";
        final Set<String> dict = Set.of("Cat", "mat", "Ca", "tm", "at", "C", "Dog", "og", "Do");
        final Collection<Collection<String>> result = ForkJoinPool.commonPool().invoke(new Single(dict, s));
        result.forEach(it -> {
            final String collect = it.stream().collect(Collectors.joining(" + "));
            System.out.println(s + " = " + collect);
        });
        System.out.println(result.size());
    }

    /*
     * @param : A string
     * @param : A set of word
     * @return: the number of possible sentences.
     */
    public int wordBreak3(String s, Set<String> dict) {
        // Write your code here
        return ForkJoinPool.commonPool().invoke(new Single(dict, s)).size();
    }

    private static class Single extends RecursiveTask<Collection<Collection<String>>> {
        private final Collection<String> dict;
        private final Collection<String> prefix;
        private final String s;

        private Single(Collection<String> dict, String s) {
            this(dict, new ArrayList<>(), s);
        }

        private Single(Collection<String> dict, Collection<String> prefix, String s) {
            this.s = s;
            this.dict = dict;
            this.prefix = prefix;
        }

        @Override
        protected Collection<Collection<String>> compute() {
            if (s.isBlank()) {
                return List.of(prefix);
            }
            final Collection<Single> tasks = new ArrayList<>();
            final int len = s.length();
            for (int i = 1; i <= len; i++) {
                final String sub = s.substring(0, i);
                if (!dict.contains(sub)) {
                    continue;
                }
                final String other = s.substring(i);
                final Collection<String> newPrefix = new ArrayList(prefix);
                newPrefix.add(sub);
                final Single task = new Single(dict, newPrefix, other);
                tasks.add(task);
            }
            if (tasks.isEmpty()) {
                return new ArrayList<>();
            }
            invokeAll(tasks);
            return tasks.parallelStream()
                    .map(Single::join)
                    .flatMap(Collection::parallelStream)
                    .collect(Collectors.toList());
        }
    }
}
