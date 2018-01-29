package com.bus.chelaile.util;



import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A static helper tool for creating collections(list, map, set).
 * 
 * @author chinafzy1978@gmail.com
 *
 */
public class New {

	public static <T> List<T> arrayList() {
		return new ArrayList<T>();
	}

	public static <T> List<T> arrayList(Collection<? extends T> objs) {
		return new ArrayList<T>(objs);
	}

	public static <T> List<T> arrayList(int size) {
		return new ArrayList<T>(size);
	}

	public static <K, V> ConcurrentHashMap<K, V> concurentMap() {
		return new ConcurrentHashMap<K, V>();
	}

	public static <K, V> ConcurrentHashMap<K, V> concurentMap(int size) {
		return new ConcurrentHashMap<K, V>(size);
	}

	public static <T> List<T> concurrentList() {
		return new CopyOnWriteArrayList<T>();
	}

	public static <T> List<T> concurrentList(Collection<? extends T> objs) {
		return new CopyOnWriteArrayList<T>(objs);
	}

	public static <K, V> ConcurrentHashMap<K, V> concurrentMap(Map<? extends K, ? extends V> m) {
		return new ConcurrentHashMap<K, V>(m);
	}

	/**
	 * Return a CopyOnWriteArrayList. 
	 * 
	 * @return
	 */
	public static <T> CopyOnWriteArrayList<T> copyOnWriteList() {
		return new CopyOnWriteArrayList<T>();
	}

	public static <T> CopyOnWriteArrayList<T> copyOnWriteList(Collection<? extends T> objs) {
		return new CopyOnWriteArrayList<T>(objs);
	}

	public static <K, V> HashMap<K, V> hashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> HashMap<K, V> hashMap(int size) {
		return new HashMap<K, V>(size);
	}

	public static <K, V> HashMap<K, V> hashMap(Map<? extends K, ? extends V> obj) {
		return new HashMap<K, V>(obj);
	}

	public static <T> Set<T> hashSet() {
		return new HashSet<T>();
	}

	public static <T> Set<T> hashSet(Collection<? extends T> objs) {
		return new HashSet<T>(objs);
	}

	public static <T> Set<T> hashSet(int size) {
		return new HashSet<T>(size);
	}

	public static <K, V> LinkedHashMap<K, V> linkedMap() {
		return new LinkedHashMap<K, V>();
	}

	public static <K, V> LinkedHashMap<K, V> linkedMap(int size) {
		return new LinkedHashMap<K, V>(size);
	}

	public static <K, V> LinkedHashMap<K, V> linkedMap(Map<? extends K, ? extends V> m) {
		return new LinkedHashMap<K, V>(m);
	}

	public static <T> LinkedHashSet<T> linkedSet() {
		return new LinkedHashSet<T>();
	}

	public static <T> LinkedHashSet<T> linkedSet(Collection<? extends T> objs) {
		return new LinkedHashSet<T>(objs);
	}

	public static <T> LinkedHashSet<T> linkedSet(int size) {
		return new LinkedHashSet<T>(size);
	}

	public static <T> ThreadLocal<T> theadLocal() {
		return new ThreadLocal<T>();
	}
}
