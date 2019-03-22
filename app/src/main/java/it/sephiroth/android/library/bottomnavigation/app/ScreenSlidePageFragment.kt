package it.sephiroth.android.library.bottomnavigation.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_screen_slide_page.*

class ScreenSlidePageFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View = inflater.inflate(R.layout.fragment_screen_slide_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = "<h1>One morning, when Gregor Samsa woke from troubled \n" +
                   "dreams.</h1>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>One morning, when Gregor Samsa woke from troubled \n" +
                   "dreams, he found himself transformed in his bed into \n" +
                   "a horrible vermin. He lay on his armour-like back, \n" +
                   "and if he lifted his head a little he could see his \n" +
                   "brown belly, slightly domed and divided by arches into \n" +
                   "stiff sections. The bedding was hardly able to cover \n" +
                   "<strong>strong</strong> it and seemed ready to slide \n" +
                   "off any moment. His many legs, pitifully thin \n" +
                   "compared with the size of the rest of him, \n" +
                   "<a class=\"external ext\" href=\"#\">link</a> waved about \n" +
                   "helplessly as he looked. \"What's happened to me? \" he \n" +
                   "thought. It wasn't a dream. His room, a proper human \n" +
                   "room although a little too small, lay peacefully \n" +
                   "between its four familiar walls.</p>\n" +
                   "<br />" +
                   "<br />" +
                   "<h1>One morning, when Gregor Samsa woke from troubled \n" +
                   "dreams.</h1>\n" +
                   "<br />" +
                   "<br />" +
                   "<h2>The bedding was hardly able to cover it.</h2>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>It showed a lady fitted out with a fur hat and fur \n" +
                   "boa who sat upright, raising a heavy fur muff that \n" +
                   "covered the whole of her lower arm towards the \n" +
                   "viewer.</p>\n" +
                   "<br />" +
                   "<br />" +
                   "<h2>The bedding was hardly able to cover it.</h2>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>It showed a lady fitted out with a fur hat and fur \n" +
                   "boa who sat upright, raising a heavy fur muff that \n" +
                   "covered the whole of her lower arm towards the \n" +
                   "viewer.</p>\n" +
                   "<br />" +
                   "<br />" +
                   "<ul>\n" +
                   "  <li>Lorem ipsum dolor sit amet consectetuer.</li>\n" +
                   "  <li>Aenean commodo ligula eget dolor.</li>\n" +
                   "  <li>Aenean massa cum sociis natoque penatibus.</li>\n" +
                   "</ul>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>It showed a lady fitted out with a fur hat and fur \n" +
                   "boa who sat upright, raising a heavy fur muff that \n" +
                   "covered the whole of her lower arm towards the \n" +
                   "viewer.</p>\n" +
                   "<br />" +
                   "<br />" +
                   "<form action=\"#\" method=\"post\">\n" +
                   "  <fieldset>\n" +
                   "    <label for=\"name\">Name:</label>\n" +
                   "    <input type=\"text\" id=\"name\" placeholder=\"Enter your \n" +
                   "full name\" />\n" +
                   "<br />" +
                   "    <label for=\"email\">Email:</label>\n" +
                   "    <input type=\"email\" id=\"email\" placeholder=\"Enter \n" +
                   "your email address\" />\n" +
                   "<br />" +
                   "    <label for=\"message\">Message:</label>\n" +
                   "    <textarea id=\"message\" placeholder=\"What's on your \n" +
                   "mind?\"></textarea>\n" +
                   "<br />" +
                   "    <input type=\"submit\" value=\"Send message\" />\n" +
                   "<br />" +
                   "  </fieldset>\n" +
                   "</form>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>It showed a lady fitted out with a fur hat and fur \n" +
                   "boa who sat upright, raising a heavy fur muff that \n" +
                   "covered the whole of her lower arm towards the \n" +
                   "viewer.</p>\n" +
                   "<br />" +
                   "<br />" +
                   "<table class=\"data\">\n" +
                   "  <tr>\n" +
                   "    <th>Entry Header 1</th>\n" +
                   "    <th>Entry Header 2</th>\n" +
                   "    <th>Entry Header 3</th>\n" +
                   "    <th>Entry Header 4</th>\n" +
                   "  </tr>\n" +
                   "  <tr>\n" +
                   "    <td>Entry First Line 1</td>\n" +
                   "    <td>Entry First Line 2</td>\n" +
                   "    <td>Entry First Line 3</td>\n" +
                   "    <td>Entry First Line 4</td>\n" +
                   "  </tr>\n" +
                   "  <tr>\n" +
                   "    <td>Entry Line 1</td>\n" +
                   "    <td>Entry Line 2</td>\n" +
                   "    <td>Entry Line 3</td>\n" +
                   "    <td>Entry Line 4</td>\n" +
                   "  </tr>\n" +
                   "  <tr>\n" +
                   "    <td>Entry Last Line 1</td>\n" +
                   "    <td>Entry Last Line 2</td>\n" +
                   "    <td>Entry Last Line 3</td>\n" +
                   "    <td>Entry Last Line 4</td>\n" +
                   "  </tr>\n" +
                   "</table>\n" +
                   "<br />" +
                   "<br />" +
                   "<p>It showed a lady fitted out with a fur hat and fur \n" +
                   "boa who sat upright, raising a heavy fur muff that \n" +
                   "covered the whole of her lower arm towards the \n" +
                   "viewer.</p>\n"

        textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}