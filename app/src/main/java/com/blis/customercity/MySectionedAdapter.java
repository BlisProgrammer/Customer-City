package com.blis.customercity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MySectionedAdapter extends ArrayAdapter<String> implements SectionIndexer {

    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
    private List<String> items;

    public MySectionedAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.items = items;
        alphaIndexer = new HashMap<>();

        // Populate alphaIndexer
        int size = items.size();
        for (int x = 0; x < size; x++) {
            String s = items.get(x);
            String firstLetter = s.substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(firstLetter)) {
                alphaIndexer.put(firstLetter, x);
            }
        }

        // Create sections array
        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);
        Collections.sort(sectionList); // Sort sections alphabetically
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= sections.length) {
            return 0; // Handle out of bounds
        }
        return alphaIndexer.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        // Find the section for a given position
        for (int i = 0; i < sections.length; i++) {
            if (position < alphaIndexer.get(sections[i])) {
                return i - 1; // Return the previous section
            }
        }
        return sections.length - 1; // Last section
    }

    @Override
    public Object[] getSections() {
        return sections;
    }
}