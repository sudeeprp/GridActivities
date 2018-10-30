package com.thinklearn.tide.activitydriver

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.thinklearn.tide.interactor.ClassroomContext
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ContentInteractor
import java.io.File

val border_html_piece: HashMap<String, String> = hashMapOf(
"1_french_x" to """url('data:image/png;base64,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')""",
"1_french_y" to """url('data:image/png;base64,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')""",
"1_math_x" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAOCAMAAAEOBd4WAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIlUExURQAAAP8AAP8AgP+AgP9VVf9AgMxmZv9mZv9Jbd9gYOZNZv9NZupVautiYu1bbfBaaeNjY/JeXvJea/JZZudhYehdaPNdaOtiYu1bZO1YYeZaY+9ja+dYaPFcauxgZudZZe9cZ+peY+hbZOpcZOpcaeZfZ+pfZ+tdZehdZOldZeldaOpcZ+tfZuhdY+tdY+9eZ+xgZulbZexdZ+lcZepcZepeZ+tcZ+hbZutfZelaZexdZepdZ+tdZ+lcZulcZuleZexeZexeZ+5eZ+5gZ+pcZuxcZu9eZ/FgaetdZupeZ+teZu1eaO1eZulcZuteZ+ldZuhdZupdZuhcZeteZuldZetdZ+5eaOpdZu1faOpdZulcZepeZe5fZ+leZupdZupdZexdZ+5eaO1fZ/NhaepdZ+xeaOpcZupeZupdZuteZ+5eaO5fZ+pcZupdZ+xeZ+xeZ+tdZu1daOldZulcZutdZutdZutdZuteZ+teZ+pdZuxeZ+ldZu1eaOldZupdZupdZuteZu5fZ+tdZu9fafFgaepdZupdZuxeZ+5eaOpdZupdZu5faOxdaO1eaO1faOpeZu5faOlcZexdZu9eaO5faOpcZu1eZ/FgaupdZutdZutdZ+xdZuxeZ+1eZ+1eaO5eZ+5eaO5faO9faPBfaPBfafBgafFfafFgafJgafJgavNgavNhavRhavVhavVha/Via/Zia/ZibPdia/dibPhibPhjbPpjbcutf4EAAACYdFJOUwABAgIDBAUFBwgKCgwNDhESExMUFRYWGhwdHx8gJCgrLzE4PT0+Pj9CR0dIS01NT1BRUlNWV1laW11dY2ZnaWpqampqbGxtbXB3fX1/gIuOj4+TlpeXpqenra6ur7GyuLi4uru/wMHDxszOz9LT09rb3d/g4OHi4uTm5ufn6enr7e3u7u7x8vLz9PX19vb3+Pj5+fn6+/z91AzRbgAAAAlwSFlzAAAOwwAADsMBx2+oZAAAAZFJREFUGFdVkOdTE3EQhh+6CoIoSlBpCgQULBQVFKKEIk1FwIJdpBcbVpoGRVCK3QA5zV2CR3InUSD+ff4SwBneDzu7z7y7O/NyhFMFwCSYL3AJGVoR9c48Bz+CF11nwYUJnsJjeMErPqTRiwV+ir0Fpnive+Od53kwLZbHbc+sDWRLXbY2sAgAEUqLqAO2lFXwWSosw6NG3nNjNsWC8W7zJgi6dX8PeKxGkuQxaRHUL6+1UKIlI1eGOWZL5FBUoiMSgwBC3e4QCP/7klVQqRz14c2O2hVQIDWW7trvPp0sXyZuMZ19Xl3/dYCeOcUZzEMxeNN9/hOfNKdT1W9uEX1wvUtzObTRDMivejsr+86eXVLe7N16fc4+uB2uLf+uCc38qvRtE18tkt8QYzDsCKBCk0Z2++4RWNQhfzsjmjWDX3WqPJG60m6o7fzx/XnCOkOJbO8XXyG36fbFjXBOU0Vk/w1Z7+xWqzTv/pNF2A3PzPSTMXnGlSeCPV5sFvkFHS43+XXSvFO4Y6rbhx5dzQngH09gffwVkE/RAAAAAElFTkSuQmCC')""",
"1_math_y" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAgCAMAAAFiN4kzAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIlUExURQAAAP8AAP8AgP+AgP9VVf9AgMxmZv9mZv9Jbd9gYOZNZv9NZupVautiYu1bbfBaaeNjY/JeXvJea/JZZudhYehdaPNdaOtiYu1bZO1YYeZaY+9ja+dYaPFcauxgZudZZe9cZ+peY+hbZOpcZOpcaeZfZ+pfZ+tdZehdZOldZeldaOpcZ+tfZuhdY+tdY+9eZ+xgZulbZexdZ+lcZepcZepeZ+tcZ+hbZutfZelaZexdZepdZ+tdZ+lcZulcZuleZexeZexeZ+5eZ+5gZ+pcZuxcZu9eZ/FgaetdZupeZ+teZu1eaO1eZulcZuteZ+ldZuhdZupdZuhcZeteZuldZetdZ+5eaOpdZu1faOpdZulcZepeZe5fZ+leZupdZupdZexdZ+5eaO1fZ/NhaepdZ+xeaOpcZupeZupdZuteZ+5eaO5fZ+pcZupdZ+xeZ+xeZ+tdZu1daOldZulcZutdZutdZutdZuteZ+teZ+pdZuxeZ+ldZu1eaOldZupdZupdZuteZu5fZ+tdZu9fafFgaepdZupdZuxeZ+5eaOpdZupdZu5faOxdaO1eaO1faOpeZu5faOlcZexdZu9eaO5faOpcZu1eZ/FgaupdZutdZutdZ+xdZuxeZ+1eZ+1eaO5eZ+5eaO5faO9faPBfaPBfafBgafFfafFgafJgafJgavNgavNhavRhavVhavVha/Via/Zia/ZibPdia/dibPhibPhjbPpjbcutf4EAAACYdFJOUwABAgIDBAUFBwgKCgwNDhESExMUFRYWGhwdHx8gJCgrLzE4PT0+Pj9CR0dIS01NT1BRUlNWV1laW11dY2ZnaWpqampqbGxtbXB3fX1/gIuOj4+TlpeXpqenra6ur7GyuLi4uru/wMHDxszOz9LT09rb3d/g4OHi4uTm5ufn6enr7e3u7u7x8vLz9PX19vb3+Pj5+fn6+/z91AzRbgAAAAlwSFlzAAAOwwAADsMBx2+oZAAAAZhJREFUKFNVkfk7VFEYxz+ypGhRyk2hsisJrcKtpk0pRPtKtqIo2iMtkqJoHZqTuTPDNXNvBjX9fZ179Xgenx/Oe973vO/3+5xzoIpwaPAgWR7B9jvsMa393ZRlspbCNjJfEUIVYpYoLtD7dxM6AYhcj7h9EY0iqx2nEGw2vC+8mo/arytiODZu19kCS2ddgd2Hf9HWD8eFlN94SR7kkJBpkYUjGKcoyjpYTXLjTnjr+f7TGC3Eu/VWC/33MU4uil4ycpOaP1AXjLdldyyWy5oNkJvIdbcYJLAf/WqZ/kyo+FLD0Jwqnz0KvFehwprJt+cssmzfBMwfQnKQ0Alpr8RCdvPTzlNpT6b0gP90GOcN17dPmmusqbJd81/mgb81itKuKvlOD33PKfKL6TxL7Zw+OlUOEQVXMqz0RvBakhUXsvJM9/CbxnQ4MhNycMB0jk8aHu1DdsmkqfLI93gVxFZPuIVTpmcNV1/9Wjl06ItbphQP/H4tLyLJHTpqx3nk1y3EEdLmkUaqOfKu5+UcH/exq+Pe3v+NwD/8zX38KUPXGgAAAABJRU5ErkJggg==')""",
"2_french_x" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACIAAAAOCAMAAAEK8A4rAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAJSUExURQAAAAD/AICAgFWqVUCAQGaZZkmSSVWOVU2ZTVWVVU6JTk6dTlCPUEuWWk2MWU2ZWVGXXU6QWVWVVU6TWEyOVVWXVVKSUk+VT0+VWFOWU1OWWlCSV06VVVORWk+SVU2UWVGRV1GXV0+ZVVGSV06SU1KUV1CSVU+UWFGUWVGVWVGSWVCTWE6RVlGTVVGXWU+TVlGVWVCTV0+UVk6SVVKWVU6TWE+UWFCTVlCUWVGUV0+TVU+TWFGSV1GUVlGXWE6VVlCUWE+VV06TVlCUV0+TV0+UVlCVV1CTV1CWV1KWWVKYWVKaW1SaW1CTV1CUV1GUV1GUV1KWWFCTWFCTVlCUWFCTV0+TVlCUV1CXV0+TVlGTWFGVWFCUV1CWV1OYWlGUWFKWWFGVV1CTV1GUV1CUWE+TV0+VV1CTVlCVWFCUVlCVV1CTV1CUVlKYWVGTVlGWWFCVWVGWWVCUV1CTV1CVV1CUVlGVV1CVV1CUWFCUV1KWWFCUV1CUVlCVVlCVWFCUVlCVV1CUV1GWWFGUWFCTV1CUVlGXWVCUV1GVWFCUV1CUV1CVWE+UV1CUV1CUVlCUV1CTVlCUV1CUVlGUV1CUV1GWV1CUV1CUV1CUV1CUV1CUWFCUV1GWV1GUV1KXWVCUVlGVV1CTV1GUWFGVWFKXWVGVV1CUV1GWWFCTV1CUV1CUV1CVV1CWV1CWWFCUV1CVV1CVWFGVWFGWWFGWWVGXWFGXWVKXWVKYWVKYWlOZWlOaWlOaW1SaW1SbW1ScW1ScXFSdXFWdXFWeXFaeXVafXVagXjgzj9UAAACudFJOUwABAgMEBQcJCgwNDRARFBQWFxgaGxscHR0iIiMkJSorLCwtLzEyNjc5PD9AQUJCR0hJSktLTlFTVlhaWl5iYmVmZ2hpamtsbW1tbW1tb3BydXqAhoaJi4yMjo6Oj4+QkZaYnJ6goaGmpqmqrKyvsbG1t7i5ub3BwsPFx8rMzMzPz9DQ0dLS0tnZ293f4eLj5ebo6err7e7v8fP09fX39/j4+fn6+vv8/P39/v7+/iFGbUcAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAG7SURBVChTLYz5X01BGIe/UiqKS7qSpSukhOxkK6JbdqkUl+z7viVblpKsIUdliXBSlzlmOGeaOSai/i9zLu9P3/f5PJ8HGBfCCcAO5wEzkLkCJl6lAFwiLCbvAWqQuMFlsH8DIWwNAY2IboKBwFfACT4xxn5mwAD62lV2CgfQRuoAg5iOD/eJ+AVsE8PYPE3Um5hKT4CPdZtEwAhPfcuhePktzXbtABJ2hrboGXf67mJgAuVD4GcvaDNSJRfXgCufaJUm6dUm3vcMRg4gjz9mmgi6CfKSV55DS48wn58tWv5tovvcI08t0yQnk9mSEmvuIXK77Cb6jwLNA8N/KnkHOOX21Hoazv2R/SXeGPO6Vyr148NG75l0WYjvdl+9tze7hJHOLL38BgvPn/bQsTIQdUwR5/zqgjPXNc/4aD9a+cW5N/Sfsn3vO342BoUuezbKC+iLb7R5LvYJsT+ikJetjPIDg8olr/lv4KAgllScELE0UpkCNDh84fgOKg7rcFwulglmTA8EAkGHtoweqStd3Zy6D0Yg6YayHCl7r2JN0bq0SG3B2vU5sfnFwWDxqlnxERI1e/fFCxUz/wKgT62sP9qkOAAAAABJRU5ErkJggg==')""",
"2_french_y" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA0AAAAhCAMAAAFCXOGVAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIxUExURQAAAICAgID/gFWqVVWqVUmSSUCAQFWOVU2ZTV2LXV2iXVWVVU6JTluSW0uWWlWOVU2ZWVWSVVGXUVKPUlKPXEyXVVKSW02RVVKUWlWTVVOWWlCSV1OYWk+SVVGSV1KUV06TWFCSVU+UU1GVWU+SVlGUWU+TVlCWV0+UVlKWWFGUV0+VVk+UWFKUWFGVV0+VWFCSVk+TWFCWWVGSV1CSWFCVWFCVWE+VV06TVlCUV0+UWFCTV1GUV1CSVlCVVk+TVlGWWU+WVk+TV1GTV1GVV1GXWVOZWVKWWVCUVk+VV1GUWFCUWE+UV0+VVlCUV1CUV1CUV1CWWVGVWVGUWFCTVlCUV0+UVlCUV1CWWFKWWE+VWFGVWFKZWU+VVk+UV1GTV1CUV0+UV1CTV1CVV1GVWFGUWFGUWFCTV1GWWFCUV1GVWFCTV1GWWVCUVlCUWFCUV1GWWE+UV1CUWE+TV1GWV1CUV0+TV1GUV1CUV1CUV0+UV1CVV1CVV0+UWFCUV0+TV1CUV1CVV1CTV1CVWFCUV1KWWFCUV1CUV0+UV1CUV1CUWFCUV1CUV1CUV1CUV1CUV1CUV1CVV1CUV1CVV1GVV1CUVlGXWE+UV1CUV1CVV1GWWFCUV1GVWFCUV1CVV1CUV1GXWFCUV1CVV1CVWFGVWFGWWFGWWVGXWFGXWVKXWVKYWVKYWlKZWVOZWlOaWlOaW1ObW1SaW1SbW1ScW1ScXFWcXFWdXFafXlagXVagXlehXny48DcAAAChdFJOUwACAgMGBwgJCgsLDA0OERIUFRYZGRscHh8hIiMlKi8yNDY3PERFR0lKS0xNUVFSV1laXF5gYGNnaGlrb3Jzc3R1d3h4eHh4eXyEhYaKi4yNj4+QkZKSlJaWlpeXmJqboaKnqKipq66vsbK0tre6urzAwcPExMXHysvQ0dLT1NbX2dvc3d7e3+Hn6Onq6+3u7/Dw8fLz9fX3+Pj6+/v8/P7+WqMCswAAAAlwSFlzAAAOnAAADpwBB5RT3QAAAaZJREFUKFMtUAlXTVEY3aVJr0kIzaYyVAoNIo/XI2NmGTJFUTSJEjIrQ5lSCHGfe7x37u27l5N3w6/rnFv7rLO+vc/ea6+zPmzMAJ6hrhEeDRIL1uA5cNkHigO8uAKcQJKwspS3CCV8HLiNXpA8qaFkiKc6BvmIctHA28DNUsmK8PirHKwLMb+NARKuHXF2A3Doo9/GGx/qLYWd6j37AflxWruV8K5YyYU3LbYDfVeBzrvg2UC+CToo2210BIbfa5eAI7peq6IoPBeNHrvbMEYhtnw+vuQPRNPQ/WaBv6r9v5tBiryRe747WPuKzFA+LLZuvvzRI6JhS/mVY/yfmkhsx7VweJvLD4edk3jIvy1zVQE3r2M9/fiyd3F8+lFdZyuBjDuCsRD7OXkj1c0gZfnqHM8MnVe+v8JT/cuyxDGgTAQmTB58vbslRKU45fRGRN1zdiH2ieNDHelEuhYkCtg+rNi+dRZV3pyZJom4vPMjNYqkber8ZHHNmNyMfYJY0Bxs/WCrndU7WuDlxZKlb4W7wbmrLgyRwTT9xZlMpSXm5B7oN6b80+3YkeEAoC60AAAAAElFTkSuQmCC')""",
"2_math_x" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAOCAMAAAEOBd4WAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIBUExURQAAAAAAAAD/AAD//4CAgFVVVVWqVUCAQDOZZmaZZlWqVUCAYFWOVUaLXU6JYkSZVVWZVVCfUEuWWk2ZWUaLUVGXUVGXXU6QWU6TWFKSW1KbW1KUWlCXWFCZV02ZU1GVV02UU1GXV0ySV06TWE2VV0+UWE2WV06TVk+WWFCXWE6VVlCYWE+WVk+UVk2XV1CZWU6WV1GWV0+VWE6XV1CYWU+WWFCXWE2VVVCVWE+WV1CWWE+XV06XV0+VV0+YV0+YWU+aWVCZWlCWWVCWV06VVlGXV1CWVk6VWE6VVlCXWE+VV06WVk+VV1CVV1CWV0+XWFCWV0+VV0+WWE+XVk+XWE+WV06VV06WV06WVk+WWE+WV1CYWU6WV0+VVlGYWE+WVlGZWVGYWVGZWFCYWFKbWlCWWE+WV0+XV0+YWE+WVlCXWFCXWE+XV1CaWU+XV1CXV0+WV1CYWE+VVk+WV0+WWFCXWFCWWFCXWE+WV0+XWE+XWE+XWE+XWE+XV1CWV1GZWFGaWVGaWk+WV1CZWU+WV1CXV0+WV0+WV1CaWVCWV0+WV1CZWE+WVlCZWU+WV1CZWFCXWFGZWVCZWFGbWU+WV0+XV0+XWFCXWFCYWFCZWFCZWVGZWVGaWVGbWlKbWlKcWlKcW1KdW1KeXFOcW1OdW1OdXFOeW1OeXFOgXVSfXFWhXWcqlxAAAACUdFJOUwABAQECAwMEBQUGCAkLDQ8PEBEUFhYWFxocHB8gIygpKywvNDU3ODs9QEFDREpMUFVVV1hZWl1gY2RmZ2lqampqbG1wc3h5fX9/hIaHiY2Oj5WXmpqbnJyjpaipra6usbG0trq7vb7ByMnJysvM0tbY2d7e39/g4OLi4+Xn6Orq7u7v7/Dw8fP09vf5+/v8/P39/v65iqYeAAAACXBIWXMAAA7DAAAOwwHHb6hkAAABhklEQVQYV1WQ+T9VQRiHnyQ3JFcoZCkR2hQVpUulUqEoS1q0okWbFltoc6O0qHOc4U5zRl3d6q9sDvcHfX+Yed/n87zvfD7DXvbtBsagupNL2LF0gR130aV0EuZjtCasCMAA9MM4LxnYapogKDMXZoqQnsmQzTy1bPgsRq1atokHzh0IGgCZosWcfU5JFMx9qTpkZvwPXQ4H0mHXkxurDL7+rAh+WAWUiLciDF8/vVbxrBcFdL/ipPBTmp4jk9logMmIMfD9fUMUnJPFHk6STUvgmNN6dN0WXbdZXCE7UkhZROuFPG7L0Hcfz39rPV/o+SemlZRKtSd4TYN0ldQTO2H/qfcztrf2QkQOb8rsDcne1dD259eZ+O0f5kbSzKtB4QkrNmRlpcJ5N/RojbcCKu+Jb2fNHRUWk3hVyeGcpTqu8b4QL/L+E06r2cf5XrHj1s3Lfqh3Zc8yoeKjY1lC/bRyWdkRtp2hd7O2Koe1B2qqzf/5yo8EFlNzMMXY/uN3Rwev7YnhH51ieQcIxiN9AAAAAElFTkSuQmCC')""",
"2_math_y" to """url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAgCAMAAAFiN4kzAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIBUExURQAAAAAAAAD/AAD//4CAgFVVVVWqVUCAQDOZZmaZZlWqVUCAYFWOVUaLXU6JYkSZVVWZVVCfUEuWWk2ZWUaLUVGXUVGXXU6QWU6TWFKSW1KbW1KUWlCXWFCZV02ZU1GVV02UU1GXV0ySV06TWE2VV0+UWE2WV06TVk+WWFCXWE6VVlCYWE+WVk+UVk2XV1CZWU6WV1GWV0+VWE6XV1CYWU+WWFCXWE2VVVCVWE+WV1CWWE+XV06XV0+VV0+YV0+YWU+aWVCZWlCWWVCWV06VVlGXV1CWVk6VWE6VVlCXWE+VV06WVk+VV1CVV1CWV1GXWFCWV0+VV0+WWE+XVk+XWE+WV06VV06WV06WVk+WWE+WV1CYWU6WV0+VVlGYWE+WVlGZWVGYWVGZWFCYWFKbWlCWWE+WV0+XV0+YWE+WVlCXWFCXWE+XV1CaWU+XV1CXV0+WV1CYWE+VVk+WV0+WWFCXWFCWWFCXWE+WV0+XWE+XWE+XWE+XWE+XV1CWV1GZWFGaWVGaWk+WV1CZWU+WV1CXV0+WV0+WV1CaWVCWV0+WV1CZWE+WVlCZWU+WV1CZWFCXWFGZWVCZWFGbWU+WV0+XV0+XWFCXWFCYWFCZWFCZWVGZWVGaWVGbWlKbWlKcWlKcW1KdW1KeXFOcW1OdW1OdXFOeW1OeXFOgXVSfXFWhXUYq/VYAAACUdFJOUwABAQECAwMEBQUGCAkLDQ8PEBEUFhYWFxocHB8gIygpKywvNDU3ODs9QEFDREpMUFVVV1hZWl1gY2RmZ2lqampqbG1wc3h5fX9/hIaHiY2Oj5WXmpqbnJyjpaipra6usbG0trq7vb7ByMnJysvM0tbY2d7e39/g4OLi4+Xn6Orq7u7v7/Dw8fP09vf5+/v8/P39/v65iqYeAAAACXBIWXMAAA7DAAAOwwHHb6hkAAABjElEQVQoU1WQ91fUQBSFPwFZFkUWdFFcKSKgiAqK2MMqqNjA3jtWBCttFcGCHTtZMpBxEjS6yl/pJHDk+P3w7rwz9913ZuAQmjbp19wQNTfZ4/rnnqIcGyKUsOYZKQwhUoQ5z/PJpXzFt+QjBo4j2OnbMYVguZJDUnzj8sdoiOYgEpbBbE94Kxp/0vUCjlh6tuq6vlhHtNyngu3fC2Kx2BJYQMmNOngzPiKc5FpkdcdtXj/EaSUzYt3iwB+44kWC2FXpumTNheI8Tkq7nx9xVOdRdV8YyFLmKNPgi1wMHww4tVCbdfA0FcHeKO6o0MT5vVevj82D1e0DiYNVjz3pqMNpnHWt4bfCEtf23x1Tl+hTd8JsetCif+WRHKRWil81fto5N+nt0rryRKXfXrVPL/L1f7KbOhP3jhXBNnN0C/XKtB1HyCdlW5VrkJCDhVB4UY19NnV7QYn+M/MhY9+4pVt2f0o91Q/RbLR3BJo5KxDSQlM6Q8NE8h8TDRiu+e7lqyneb2Z9b/eGaSPwF3pDeQfQoMDWAAAAAElFTkSuQmCC')"""
)

val html_before_border_images =
"""
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>Learn</title>
<style>
html, body {
height: 100%;
}
body {
overflow: hidden;
}
.nomargins {
    margin-top: 0px;
    margin-left: 0px;
    margin-right: 0px;
    margin-bottom: 0px;
}
.in-margins {
    margin-top: 20px;
    margin-left: 20px;
    margin-bottom: 20px;
    margin-right: 20px;
}
.bigborder {
    background-image:""".trimIndent()

val html_border_to_iframe_src = """
    background-repeat: repeat-x, repeat-y, repeat-x, repeat-y;
    background-attachment: fixed;
    background-position: top, left, bottom, right;
}
</style>
</head>
<body class="nomargins">
<div class="bigborder" style="height:100%;width=100%;">
<iframe src="""".trimIndent()

val html_after_iframe_src = """" frameborder="0" style="overflow:hidden;height:95%;width:97%;position:absolute;top:16px;left:16px;"></iframe>
</div>
</body>
</html> """.trimIndent()

class CurriculumActivity : AppCompatActivity() {
    lateinit var grade: String
    lateinit var subject: String
    lateinit var chapter: String
    lateinit var activity_identifier: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        grade = intent.getStringExtra("SELECTED_GRADE")
        subject = intent.getStringExtra("SELECTED_SUBJECT")
        chapter = intent.getStringExtra("SELECTED_CHAPTER")
        activity_identifier = intent.getStringExtra("SELECTED_ACTIVITY")

        loadCurriculumActivity(grade, subject, chapter, activity_identifier)
    }
    fun loadCurriculumActivity(grade: String, subject: String, chapter: String, activity_identifier: String) {
        val contentStartPage = ContentInteractor().activity_page(grade, subject, chapter, activity_identifier)
        if(contentStartPage == "") {
            activityNotAvailable(activity_identifier)
        } else if(contentStartPage.toLowerCase().endsWith("pdf")) {
            loadPDF(contentStartPage)
        } else if(contentStartPage.toLowerCase().endsWith("mp4")) {
            loadVideo(contentStartPage)
        } else {
            loadIframe(contentStartPage)
        }
    }
    fun activityNotAvailable(activity_identifier: String) {
        Toast.makeText(this, resources.getString(R.string.no_tab_activity) + activity_identifier, Toast.LENGTH_LONG).show()
        finish()
    }
    fun loadVideo(contentStartPage: String) {
        setContentView(R.layout.activity_curriculum_video)
        val videoView = findViewById<VideoView>(R.id.videoPlay)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        val uri = Uri.parse(contentStartPage)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
        videoView.setOnCompletionListener { endActivity("") }
    }
    fun loadPDF(contentStartPage: String) {
        try {
            val pdfIntent: Intent = Intent(Intent.ACTION_VIEW)
            pdfIntent.setDataAndType(Uri.fromFile(File(contentStartPage)), "application/pdf")
            pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            finish()
            startActivity(pdfIntent)
        }
        catch(e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_pdf_viewer, Toast.LENGTH_LONG).show()
        }
    }
    fun get_border_html(grade: String, subject: String): String {
        var border_html = "";
        val border_key = grade + "_" + subject
        border_html += border_html_piece[border_key + "_x"] + ",\n"
        border_html += border_html_piece[border_key + "_y"] + ",\n"
        border_html += border_html_piece[border_key + "_x"] + ",\n"
        border_html += border_html_piece[border_key + "_y"] + ";\n"
        return border_html
    }
    fun loadIframe(contentStartPage: String) {
        setContentView(R.layout.activity_curriculum_html)
        val webView = findViewById<WebView>(R.id.curriculum_page)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccessFromFileURLs = true
        //needed to enable autoplay
        webView.settings.mediaPlaybackRequiresUserGesture = false
        //not sure what the following is for... trying to avoid black pages
        webView.settings.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.addJavascriptInterface(ActivityInterface(this), "Android")
        webView.webViewClient = WebViewClient()
        val baseUrl = ContentInteractor().activity_directory(grade, subject, chapter, activity_identifier)
        val pageData = html_before_border_images + get_border_html(grade, subject) + html_border_to_iframe_src +
                "file://" + contentStartPage + html_after_iframe_src
        webView.loadDataWithBaseURL("file://" + baseUrl, pageData, null, null, null)
    }

    override fun onBackPressed() {
        val webView = findViewById<WebView>(R.id.curriculum_page)
        if(webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            endActivity("")
        }
    }
    fun endActivity(datapoint: String) {
        if (datapoint != "") {
            val studentActivityResult = Intent()
            studentActivityResult.putExtra("SELECTED_GRADE", grade)
            studentActivityResult.putExtra("SELECTED_SUBJECT", subject)
            studentActivityResult.putExtra("SELECTED_CHAPTER", chapter)
            studentActivityResult.putExtra("SELECTED_ACTIVITY", activity_identifier)
            studentActivityResult.putExtra("DATAPOINT", datapoint)
            setResult(Activity.RESULT_OK, studentActivityResult)
            finish()
        }
        ClassroomInteractor.write_activity_log("{\"activity\": \"Student activity\", " +
                "\"student_id\": \"${ClassroomContext.selectedStudent!!.id}\", " +
                "\"subject\": \"$subject\", " +
                "\"chapter\": \"$chapter\", " +
                "\"activity_identifier\": \"$activity_identifier\", " +
                "\"datapoint\": \"$datapoint\"}")
    }
}

class ActivityInterface(val curriculum_context: CurriculumActivity) {
    @JavascriptInterface
    fun activityResult(datapoint: String) {
        curriculum_context.endActivity(datapoint)
    }
}
