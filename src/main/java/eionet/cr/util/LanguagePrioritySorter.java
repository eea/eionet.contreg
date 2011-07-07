package eionet.cr.util;

public class LanguagePrioritySorter implements Comparable<LanguagePrioritySorter> {

    private double priority;
    private String languageValue;

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public String getLanguageValue() {
        return languageValue;
    }

    public void setLanguageValue(String languageValue) {
        this.languageValue = languageValue;
    }

    public int compareTo(LanguagePrioritySorter language1) {
        if (language1.getPriority() > this.getPriority()) {
            return 1;
        } else {
            return -1;
        }
    }
}
