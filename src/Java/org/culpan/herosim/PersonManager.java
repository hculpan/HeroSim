/*
 * Created on Dec 10, 2004
 *
 */
package org.culpan.herosim;

import java.util.Iterator;

/**
 * @author CulpanH
 *
 */
public interface PersonManager {
    public void add(Person p);
    public void remove(Person p);
    public Iterator<Person> personIterator();
    public boolean isActingInCurrentPhase(Person p);
    public void revalidate();
}
