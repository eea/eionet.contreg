package eionet.cr.web.action.admin.postHarvest;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;

/**
 *
 * @author jaanus
 *
 */
public final class Tabs {

    /**
     * Hide utility class constructor.
     */
    private Tabs() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @return
     */
    public static List<Tab> generate(TargetType targetType) {

        String urlBinding = PostHarvestScriptsActionBean.class.getAnnotation(UrlBinding.class).value();
        List<Tab> tabs = new ArrayList<Tab>();
        tabs.add(new Tab("All-source scripts", urlBinding, "Scripts to run for all sources", targetType == null));
        tabs.add(new Tab("Source-specific scripts", urlBinding + "?targetType=" + TargetType.SOURCE,
                "Scripts to run for specific sources", targetType != null && targetType.equals(TargetType.SOURCE)));
        tabs.add(new Tab("Type-specific scripts", urlBinding + "?targetType=" + TargetType.TYPE,
                "Scripts to run for specific types", targetType != null && targetType.equals(TargetType.TYPE)));
        return tabs;
    }
}
