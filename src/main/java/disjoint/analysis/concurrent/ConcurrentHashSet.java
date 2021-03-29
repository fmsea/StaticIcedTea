package disjoint.analysis.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Original code from 
 * http://www.java2s.com/Tutorial/Java/0140__Collections/Concurrentset.htm
 * By Ben Meadowcroft 2006
 * @author elenasherman
 *
 */
public class ConcurrentHashSet<Q> implements Set<Q> {
	
	//Piggy back off the concurrent hashMap implementation
	private ConcurrentHashMap<Q, Object> map;
	
	/*
	 * Constructor
	 */
	public ConcurrentHashSet(){
		map = new ConcurrentHashMap<Q,Object>();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public Iterator<Q> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return map.keySet().toArray(array);
	}

	@Override
	public boolean add(Q item) {
		boolean containsObj = map.containsKey(item);
		if(!containsObj){
			map.put(item, Boolean.TRUE);
		}
		return !containsObj;//if object was not added then return false, if added then return true
	}

	@Override
	public boolean remove(Object o) {
		/*we double up artument as both key and vlaue */
		return map.remove(o, Boolean.TRUE);
	}

	@Override
	public boolean containsAll(Collection<?> items) {
		return map.keySet().containsAll(items);
	}

	@Override
	public boolean addAll(Collection<? extends Q> items) {
		boolean changed = false;
		for(Q item : items){
			/*update flag determines whether set has change or not */
			changed = add(item) || changed;
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> items) {
		return map.keySet().retainAll(items);
	}

	@Override
	public boolean removeAll(Collection<?> items) {
		return map.keySet().removeAll(items);
	}

	@Override
	public void clear() {
		map.clear();
	}

}
