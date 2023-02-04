import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {
    private final int swiperBound = 5000;

    private final int swipeeBound = 1000000;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String urlPath = request.getPathInfo();
        System.out.println(urlPath);

        // check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");

        // validate url and return the response
        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid inputs");
            return;
        }
        Gson gson = new Gson();
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            SwipeDetails swipeDetails = gson.fromJson(sb.toString(), SwipeDetails.class);
            // validate swiper
            if (!(1 <= Integer.parseInt(swipeDetails.getSwiper()) &&
                    Integer.parseInt(swipeDetails.getSwiper()) <= swiperBound)){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(" User not found");
                return;
            }

            // validate swipee
            if(!(1 <= Integer.parseInt(swipeDetails.getSwipee()) &&
                    Integer.parseInt(swipeDetails.getSwipee()) <= swipeeBound)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(" User not found");
                return;
            }

            // validate comment
            if(!(1 <= swipeDetails.getComment().length() &&
                    swipeDetails.getComment().length() <= 256)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid inputs");
                return;
            }

            // write successful
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Write successful");
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid inputs");
        }

    }

    private boolean isUrlValid (String[] urlParts) {
        // check whether length equals two
        if(urlParts.length < 2) {
            return false;
        }
        // check whether equals left or right
        if(!urlParts[1].equals("left") && !urlParts[1].equals("right")) {
            return false;
        }
        return true;
    }
}
