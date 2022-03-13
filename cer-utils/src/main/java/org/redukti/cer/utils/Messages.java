package org.redukti.cer.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    /**
     * This is an interface defining a message provider. Create your own implementation to override
     * the default error message provider.
     *
     * @author Mike Harm
     */
    public interface MessageProvider {

        /**
         * Returns a textual message identified by the given messageId, parameterized by the given
         * arguments.
         *
         * @param messageId the identifier of the message
         * @param arguments the arguments to fill into the message
         */
        String getMessage(String messageId, Object[] arguments);
    }

    public static final MessageProvider messageProvider = new DefaultMessageProvider();

    /** @deprecated Use {@link #getMessageById(String messageId, Object... args)} instead */
    @Deprecated
    public static String getMessage(String messageId, Object[] arguments) {
        return messageProvider.getMessage(messageId, arguments);
    }

    public static String getMessageById(String messageId, Object... args) {
        return messageProvider.getMessage(messageId, args);
    }


    /* OPT there's a noticable delay for the first error!  Maybe it'd
     * make sense to use a ListResourceBundle instead of a properties
     * file to avoid (synchronized) text parsing.
     */
    private static class DefaultMessageProvider implements MessageProvider {
        @Override
        public String getMessage(String messageId, Object[] arguments) {
            final String defaultResource = "org.mozilla.javascript.resources.Messages";

// FIXME (dibyendu)
//            Context cx = Context.getCurrentContext();
//            Locale locale = cx != null ? cx.getLocale() : Locale.getDefault();
            Locale locale = Locale.getDefault();

            // ResourceBundle does caching.
            ResourceBundle rb = ResourceBundle.getBundle(defaultResource, locale);

            String formatString;
            try {
                formatString = rb.getString(messageId);
            } catch (java.util.MissingResourceException mre) {
                throw new RuntimeException(
                        "no message resource found for message property " + messageId);
            }

            /*
             * It's OK to format the string, even if 'arguments' is null;
             * we need to format it anyway, to make double ''s collapse to
             * single 's.
             */
            MessageFormat formatter = new MessageFormat(formatString);
            return formatter.format(arguments);
        }
    }
}
