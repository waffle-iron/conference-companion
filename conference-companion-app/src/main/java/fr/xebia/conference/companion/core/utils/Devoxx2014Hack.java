package fr.xebia.conference.companion.core.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import fr.xebia.conference.companion.R;
import fr.xebia.conference.companion.core.network.JacksonConverter;
import fr.xebia.conference.companion.model.Talk;

public class Devoxx2014Hack {

    public static Collection<Talk> generateKeynotes(Context context) {
        try {
            InputStream missingTalksInputStream = context.getResources().openRawResource(R.raw.devoxx_2014_missing_talks);
            return Arrays.asList(JacksonConverter.OBJECT_MAPPER.readValue(missingTalksInputStream, Talk[].class));
        } catch (IOException e) {
            // Will never happen
            return new ArrayList<>();
        }
    }
}
