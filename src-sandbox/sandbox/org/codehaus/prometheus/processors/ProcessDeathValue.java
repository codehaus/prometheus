package org.codehaus.prometheus.processors;

/**
 * An implementation of {@link ProcessDeath}.
 *
 * @author Peter Veentjer
 */
public class ProcessDeathValue implements ProcessDeath{

    public final static ProcessDeathValue INSTANCE = new ProcessDeathValue();

    @Override
    public boolean equals(Object that){
        return that instanceof ProcessDeathValue;
    }

    @Override
    public int hashCode(){
       return 0;
    }

    @Override
    public String toString(){
        return "processdeath";
    }
}
