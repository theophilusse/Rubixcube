package com.rubix.rubix;

public class Tileset16
{
    private int[] tilemap;
    private int[] tilemapClockwise90;
    private int tilemapSize;
    private int tilemapWidth;
    private int colorFont;
    private int colorBackground;

    public Tileset16(int colorFont, int colorBackground)
    {
        this.colorFont = colorFont;
        this.colorBackground = colorBackground;
        tilemapWidth = 128 * 16;
        tilemapSize = (tilemapWidth) * 16;
        tilemap = new int[tilemapSize];
        tilemapClockwise90 = new int[tilemapSize];
        initTiles();
        System.gc();
    }

    public void setColorFont(int color)
    {
        colorFont = color;
        //initTiles(); // TODO DEBUG
        System.gc();
    }

    public void setColorBackground(int color)
    {
        colorBackground = color;
        //initTiles(); // TODO DEBUG
        System.gc();
    }

    private void setTile(int[] tilemap, String symbol, int index, boolean rotate)
    {
        int             from;
        int             to;
        int             i;

        if (symbol.length() != 256 || index < 0 || index >= 128)
            return;
        from = index * 16;
        to = from + 16;
        i = 0;
        if (!rotate)
        {
            for (int y = 0; y < 16; y++)
                for (int x = from; x < to; x++)
                    if (symbol.charAt(i++) == '.')
                        tilemap[(y * tilemapWidth) + x] = colorFont;
        }
        else
        {
            symbol = revSymbol(symbol);
            for (int y = 0; y < 16; y++)
                for (int x = from; x < to; x++)
                    if (symbol.charAt(i++) == '.')
                        tilemap[(y * tilemapWidth) + x] = colorFont;
        }
    }

    public String revSymbol(String input)
    {
        char[]          s;
        char[]          ret;

        s = input.toCharArray();
        ret = input.toCharArray();
        for (int i = 0; i < 256; i++)
            ret[i] = s[i];
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                ret[(x * 16) + ((16-1) - y)] = s[(y * 16) + x];
        return (new String(ret));
    }

    public int[] printVertical(int[] surface, int x, int y, int width, int height, String input)
    {
        int         inputLength;
        int         surfaceSize;
        int         symbolOffset;
        int         horizontalOffset;
        int         verticalOffset;
        int         lineOffset;
        int         surfaceOffset;
        int         i;
        int         j;
        char        c;

        int lineAccumulator;
        x = x % width;
        y = y % height;
        horizontalOffset = x;
        lineOffset = y * width;
        lineAccumulator = lineOffset;
        inputLength = input.length();
        surfaceSize = width * height;
        for (int index = 0; index < inputLength; index++) // Pour chaque caracteres
        {
            c = input.charAt(index); // On le place dans le char temporaire c
            if (c >= 128)
                continue; // Skip si non conforme
            else if (c != '\n') // Si ce n'est pas un retour a la ligne
            {
                symbolOffset = c * 16; // Offset X dans la table de la TILE c
                j = -1;
                while (++j < 16) // Pour chaques lignes de la TILE C
                {
                    if (j + y >= height) // Si la position du pixel depasse verticalement
                        break; // Alors on skip ce caractere
                    i = -1;
                    verticalOffset = lineAccumulator;
                    while (++i < 16) // Pour chaques pixels I sur l'axe horizontal de la TILE C
                    {
                        verticalOffset += width;
                        surfaceOffset = verticalOffset + horizontalOffset - j; // Locale->Globale
                        if (surfaceOffset < surfaceSize)
                            surface[surfaceOffset] = tilemap[(j * tilemapWidth) + symbolOffset + i];
                        else
                            break;
                    }
                }
                lineAccumulator += 16 * width;
                if (lineAccumulator >= surfaceSize)
                {
                    lineAccumulator = lineOffset - 16;
                    if (lineAccumulator < 0)
                        return (surface);
                }
            }
            else
            {
                horizontalOffset = 0;
                lineOffset += (width * 16);
                if (lineOffset >= surfaceSize)
                    return (surface);
            }
        }
        return (surface);
    }

    public int[] printVerticalTextVertically(int[] surface, int x, int y, int width, int height, String input)
    {
        int         inputLength;
        int         surfaceSize;
        int         symbolOffset;
        int         horizontalOffset;
        int         verticalOffset;
        int         lineOffset;
        int         surfaceOffset;
        int         i;
        int         j;
        char        c;

        x = x % width;
        y = y % height;
        horizontalOffset = x;
        lineOffset = y * width;
        inputLength = input.length();
        surfaceSize = width * height;
        for (int index = 0; index < inputLength; index++) // Pour chaque caracteres
        {
            c = input.charAt(index); // On le place dans le char temporaire c
            if (c >= 128)
                continue; // Skip si non conforme
            else if (c != '\n') // Si ce n'est pas un retour a la ligne
            {
                symbolOffset = c * 16; // Offset X dans la table de la TILE c
                j = -1;
                while (++j < 16) // Pour chaques lignes de la TILE C
                {
                    if (j + y >= height) // Si la position du pixel depasse verticalement
                        break; // Alors on skip ce caractere
                    i = -1;
                    verticalOffset = lineOffset;
                    while (++i < 16) // Pour chaques pixels I sur l'axe horizontal de la TILE C
                    {
                        verticalOffset += width; // TODO test
                        surfaceOffset = verticalOffset + horizontalOffset - j; // Locale->Globale
                        if (surfaceOffset < surfaceSize) // && i + from < tilemapWidth)
                            surface[surfaceOffset] = tilemap[(j * tilemapWidth) + symbolOffset + i];
                        else
                            break;
                    }
                }
                // TODO ------------------------- RENVERSER
                //horizontalOffset += 16;
                horizontalOffset -= 16;
                if (horizontalOffset < 16)
                {
                    horizontalOffset = width;
                    lineOffset += (width * 16);
                    if (lineOffset >= surfaceSize)
                        return (surface);
                }
            }
            else
            {
                horizontalOffset = 0;
                lineOffset += (width * 16);
                if (lineOffset >= surfaceSize)
                    return (surface);
            }
        }
        return (surface);
    }

    public int[] print(int[] surface, int x, int y, int width, int height, String input, int bgMask)
    {
        int[] ret;
        int bgColor;

        bgColor = colorBackground;
        colorBackground = bgColor & bgMask; // TODO Bug !!!!!!!!
        ret = print(surface, x, y, width, height, input);
        colorBackground = bgColor;
        return (ret);
    }

    public int[] print(int[] surface, int x, int y, int width, int height, String input)
    {
        int         inputLength;
        int         surfaceSize;
        int         symbolOffset;
        int         horizontalOffset;
        int         verticalOffset;
        int         lineOffset;
        int         surfaceOffset;
        int         i;
        int         j;
        char        c;

        x = x % width;
        y = y % height;
        horizontalOffset = x;
        lineOffset = y * width;
        inputLength = input.length();
        surfaceSize = width * height;
        for (int index = 0; index < inputLength; index++)
        {
            c = input.charAt(index);
            if (c >= 128)
                continue;
            else if (c != '\n')
            {
                symbolOffset = c * 16;
                j = -1;
                while (++j < 16) // Read tile and write to buffer
                {
                    verticalOffset = lineOffset + (j * width);
                    if (j + y >= height)
                        break;
                    i = -1;
                    while (++i < 16)
                    {
                        surfaceOffset = verticalOffset + horizontalOffset + i;
                        if (surfaceOffset < surfaceSize) // && i + from < tilemapWidth)
                            surface[surfaceOffset] = tilemap[(j * tilemapWidth) + symbolOffset + i];
                        else
                            break;
                    }
                }
                horizontalOffset += 16;
                if (horizontalOffset >= width)
                {
                    horizontalOffset = 0;
                    lineOffset += (width * 16);
                    if (lineOffset >= surfaceSize)
                        return (surface);
                }
            }
            else
            {
                horizontalOffset = 0;
                lineOffset += (width * 16);
                if (lineOffset >= surfaceSize)
                    return (surface);
            }
        }
        return (surface);
    }

    private void initTiles()
    {
        int         index;

        for (int i = 0; i < tilemapSize; i++)
            tilemap[i] = colorBackground;
        for (int i = 0; i < tilemapSize; i++)
            tilemapClockwise90[i] = colorBackground;
        for (boolean rotate = false; rotate != true; rotate = true) {
            /*
            for (int k = 0; k < 128; k++)
                setTile(rotate ? tilemapClockwise90 : tilemap, charset_default_tile, k, rotate);
            */

            /* Ponctuation */
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_space, 32, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_exclam, 33, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_doublequote, 34, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_hashtag, 35, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_dollar, 36, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_modulo, 37, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_amperstand, 38, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_quote, 39, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_open_parenthese, 40, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_close_parenthese, 41, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_asterix, 42, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_plus, 43, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_comma, 44, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_minus, 45, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_dot, 46, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_slash, 47, rotate);

            /* Digits */
            index = 48;
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_0, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_1, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_2, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_3, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_4, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_5, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_6, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_7, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_8, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_num_9, index, rotate);

            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_colon, 58, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_semicol, 59, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_lower, 60, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_equal, 61, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_greater, 62, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_ponc_interrog, 63, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_at, 64, rotate);

            /* Uppercase */
            index = 65;
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upra, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprb, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprc, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprd, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upre, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprf, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprg, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprh, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upri, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprj, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprk, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprl, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprm, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprn, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upro, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprp, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprq, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprr, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprs, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprt, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upru, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprv, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprw, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprx, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_upry, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_uprz, index, rotate);

            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_open_bracket, 91, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_antislash, 92, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_close_bracket, 93, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_circumflex, 94, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_underscore, 95, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_graveacc, 96, rotate);

            /* Lowercase */
            index = 97;
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowa, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowb, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowc, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowd, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowe, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowf, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowg, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowh, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowi, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowj, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowk, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowl, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowm, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lown, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowo, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowp, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowq, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowr, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lows, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowt, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowu, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowv, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_loww, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowx, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowy, index++, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_lowz, index, rotate);

            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_open_brace, 123, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_pipe, 124, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_close_brace, 125, rotate);
            setTile(rotate ? tilemapClockwise90 : tilemap, charset_spec_tilde, 126, rotate);
        }
    }

    public String       export(int scaleFactor)
    {
        String      c;
        String      export;
        int         offset;

        scaleFactor = Math.abs(scaleFactor);
        scaleFactor += scaleFactor == 0 ? 1 : 0;
        export = "/* Font made by ttrossea ("+(16*scaleFactor)+"x"+(16*scaleFactor)+"px) */";
        for (int t = 0; t < 128; t++)
        {
            export += "static public String charset_" + t + " = ";
            offset = t * 16;
            for (int l = 0; l < scaleFactor; l++)
                for (int j = 0; j < 16; j++)
                {
                    export += "\n\t\t\"";
                    for (int i = 0; i < 16; i++)
                    {
                        c = tilemap[j * tilemapWidth + offset + i] == colorFont ? "." : "0";
                        for (int k = 0; k < scaleFactor; k++)
                            export += c;
                    }
                    export += "\"+";
                }
        }
        return (export);
    }

    public String           toString()
    {
        return (export(0));
    }

    /*
            16*16 tiles
     */
    /* LOWERCASE */

    static public String charset_lowa =
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000..........000"+
                "00.0000000000.00"+
                "0000000000000.00"+
                "000...........00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "000............0"+
                "0000000000000000";

    static public String charset_lowb =
                "0000000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00...........000"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00...........000"+
                "0000000000000000";

    static public  String charset_lowc =
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000...........00"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "000...........00"+
                "0000000000000000";

    static public  String charset_lowd =

                "0000000000000000"+
                "0000000000000.00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "000...........00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "000...........00"+
                "0000000000000000";



    static public  String charset_lowe =

                /* pixels */
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000..........000"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00...........000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "000...........00"+
                "0000000000000000";



    static public  String charset_lowf =
                "0000000000000000"+
                "0000000000000000"+
                "0000.......00000"+
                "000.0000000.0000"+
                "00.000000000.000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.00.0000000000"+
                "0.....0000000000"+
                "00.00.0000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "0...000000000000"+
                "0000000000000000";



    static public  String charset_lowg =
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00000000000000.0"+
                "000..........0.0"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.000000000..00"+
                "000.........0.00"+
                "0000000000000.00"+
                "000.000000000.00"+
                "0000.........000"+
                "0000000000000000";



    static public  String charset_lowh =

                "0000000000000000"+
                "0..0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.000....000000"+
                "00....0000..0000"+
                "00.000000000.000"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "0..000000000..00"+
                "0000000000000000";



    static public  String charset_lowi =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000..0000000"+
                "0000000..0000000"+
                "0000000000000000"+
                "000000...0000000"+
                "0000000..0000000"+
                "0000000..0000000"+
                "0000000..0000000"+
                "0000000..0000000"+
                "0000000..0000000"+
                "000000....000000"+
                "0000000000000000";



    static public  String charset_lowj =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000000000000..00"+
                "000000000000..00"+
                "0000000000000000"+
                "00000000000...00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "0000000000000.00"+
                "000..00000000.00"+
                "000.00000000.000"+
                "0000.000000.0000"+
                "00000.0000.00000"+
                "000000....000000"+
                "0000000000000000";



    static public  String charset_lowk =

                "0000000000000000"+
                "0..0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000..0000"+
                "00.00000..000000"+
                "00.000..00000000"+
                "00.0..0000000000"+
                "00...00000000000"+
                "00.00..000000000"+
                "00.0000...000000"+
                "00.0000000..0000"+
                "00.000000000.000"+
                "0..000000000..00"+
                "0000000000000000";



    static public  String charset_lowl =

                "0000000000000000"+
                "0000000000000000"+
                "00000000.0000000"+
                "000.....00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "0000......000000"+
                "0000000000000000";



    static public  String charset_lowm =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00...........000"+
                "00.000...000..00"+
                "00.0000.0000..00"+
                "00.0000.0000..00"+
                "00.000000000..00"+
                "00.000000000..00"+
                "00.000000000..00"+
                "0000000000000000";



    static public  String charset_lown =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00..........0000"+
                "00.000000000..00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "0000000000000000";



    static public  String charset_lowo =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000..........000"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "000..........000"+
                "0000000000000000";



    static public  String charset_lowp =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0.0........00000"+
                "00.00000000.0000"+
                "00.000000000.000"+
                "00.000000000.000"+
                "00..0000000.0000"+
                "00.0.......00000"+
                "00.0000000000000"+
                "0...000000000000"+
                "0000000000000000";



    static public  String charset_lowq =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000.........0.0"+
                "000.000000000.00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "000.00000000..00"+
                "0000........0.00"+
                "0000000000000.00"+
                "000000000000...0"+
                "0000000000000000";



    static public  String charset_lowr =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0.0........00000"+
                "00.00000000.0000"+
                "00.000000000.000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "00.0000000000000"+
                "0...000000000000"+
                "0000000000000000";



    static public  String charset_lows =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "000.........0000"+
                "00.000000000.000"+
                "00.0000000000000"+
                "000.000000000000"+
                "0000........0000"+
                "000000000000.000"+
                "00.000000000.000"+
                "000.........0000"+
                "0000000000000000";

    static public  String charset_lowt =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000.00000000"+
                "000000..00000000"+
                "00000.....000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..00000000"+
                "000000..000.0000"+
                "000000..00.00000"+
                "0000000...000000"+
                "0000000000000000";

    static public  String charset_lowu =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00.000000000..00"+
                "00.000000000..00"+
                "00.000000000..00"+
                "00.000000000..00"+
                "00.000000000..00"+
                "000.0000000...00"+
                "0000........00.0"+
                "0000000000000000";

    static public  String charset_lowv =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00.000000000..00"+
                "00.000000000..00"+
                "000.0000000..000"+
                "0000.00000..0000"+
                "00000.000..00000"+
                "000000.0..000000"+
                "0000000..0000000"+
                "0000000000000000";

    static public  String charset_loww =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0..000000000..00"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "00.0000..0000.00"+
                "00.00..00..00.00"+
                "00...000000...00"+
                "00..00000000..00"+
                "0000000000000000";

    static public  String charset_lowx =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0..0000000000..0"+
                "000..000000..000"+
                "00000..00..00000"+
                "0000000..0000000"+
                "00000..00..00000"+
                "000..000000..000"+
                "0..0000000000..0"+
                "0000000000000000";

    static public  String charset_lowy =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "00.0000000000.00"+
                "00.0000000000.00"+
                "000.000000000.00"+
                "0000..........00"+
                "0000000000000.00"+
                "000.00000000.000"+
                "0000........0000"+
                "0000000000000000";

    static public  String charset_lowz =

                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0000000000000000"+
                "0..............0"+
                "00000000000..000"+
                "000000000..00000"+
                "0000000..0000000"+
                "00000..000000000"+
                "000..00000000000"+
                "0..............0"+
                "0000000000000000";


    /* UPPPERCASE */

    static public String charset_upra =

                    "0000000000000000"+
                    "0000......000000"+
                    "00000.0000.00000"+
                    "0000.000000.0000"+
                    "000.00000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00............00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0...00000000...0"+
                    "0000000000000000";

    static public String charset_uprb =

                    "0000000000000000"+
                    "0...........0000"+
                    "00.000000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.000000000.000"+
                    "00...........000"+
                    "00.000000000..00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0............000"+
                    "0000000000000000";

    static public String charset_uprc =

                    "0000000000000000"+
                    "000...........00"+
                    "00.0000000000.00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000.00"+
                    "000...........00"+
                    "0000000000000000";

    static public String charset_uprd =

                    "0000000000000000"+
                    "0............000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0............000"+
                    "0000000000000000";

    static public String charset_upre =

                    "0000000000000000"+
                    "0.............00"+
                    "00.0000000000.00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.000000.000000"+
                    "00........000000"+
                    "00.000000.000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000.00"+
                    "0.............00"+
                    "0000000000000000";

    static public String charset_uprf =

                    "0000000000000000"+
                    "0.............00"+
                    "00.0000000000.00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.000000.000000"+
                    "00........000000"+
                    "00.000000.000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "0...000000000000"+
                    "0000000000000000";

    static public String charset_uprg =

                    "0000000000000000"+
                    "0000........0000"+
                    "000.00000000..00"+
                    "00.0000000000.00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000.......00"+
                    "00.0000.00000.00"+
                    "00.0000000000.00"+
                    "00.000000000..00"+
                    "00.00000000...00"+
                    "000.000000.00.00"+
                    "0000......000.00"+
                    "0000000000000000";

    static public String charset_uprh =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00............00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0...00000000...0"+
                    "0000000000000000";

    static public String charset_upri =

                    "0000000000000000"+
                    "000000....000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000....000000"+
                    "0000000000000000";

    static public String charset_uprj =

                    "0000000000000000"+
                    "000000000000...0"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "00...00000000.00"+
                    "000.00000000.000"+
                    "0000.000000.0000"+
                    "00000.0000.00000"+
                    "000000....000000"+
                    "0000000000000000";

    static public String charset_uprk =

                    "0000000000000000"+
                    "0...000000....00"+
                    "00.0000000.00000"+
                    "00.000000.000000"+
                    "00.00000.0000000"+
                    "00.0000.00000000"+
                    "00.000.000000000"+
                    "00.00.0000000000"+
                    "00.0.0.000000000"+
                    "00..000.00000000"+
                    "00.00000.0000000"+
                    "00.000000.000000"+
                    "00.0000000.00000"+
                    "00.00000000.0000"+
                    "0...0000000...00"+
                    "0000000000000000";

    static public String charset_uprl =

                    "0000000000000000"+
                    "0...000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0.............00"+
                    "0000000000000000";

    static public String charset_uprm =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "00..0000000.0.00"+
                    "00.0.00000.00.00"+
                    "00.00.000.000.00"+
                    "00.000.0.0000.00"+
                    "00.0000.00000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0..0000000000..0"+
                    "0000000000000000";

    static public String charset_uprn =

                    "0000000000000000"+
                    "0..000000000...0"+
                    "00.0000000000.00"+
                    "00..000000000.00"+
                    "00.0.00000000.00"+
                    "00.00.0000000.00"+
                    "00.000.000000.00"+
                    "00.0000.00000.00"+
                    "00.00000.0000.00"+
                    "00.000000.000.00"+
                    "00.0000000.00.00"+
                    "00.00000000.0.00"+
                    "00.000000000..00"+
                    "00.0000000000.00"+
                    "0...000000000..0"+
                    "0000000000000000";

    static public String charset_upro =

                    "0000000000000000"+
                    "0000........0000"+
                    "000.00000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000";

    static public String charset_uprp =

                    "0000000000000000"+
                    "0...........0000"+
                    "00.000000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.000000000.000"+
                    "00.........00000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "0...000000000000"+
                    "0000000000000000";

    static public String charset_uprq =

                    "0000000000000000"+
                    "0000........0000"+
                    "000.00000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.000000..00.00"+
                    "00.00000000.0.00"+
                    "000.00000000.000"+
                    "0000........0..0"+
                    "0000000000000000";

    static public String charset_uprr =

                    "0000000000000000"+
                    "0..........00000"+
                    "00.00000000.0000"+
                    "00.000000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.000000000.000"+
                    "00.0000000.00000"+
                    "00........000000"+
                    "00.000000.000000"+
                    "00.0000000.00000"+
                    "00.00000000.0000"+
                    "00.000000000.000"+
                    "00.0000000000.00"+
                    "0...00000000...0"+
                    "0000000000000000";

    static public String charset_uprs =

                    "0000000000000000"+
                    "0000..........00"+
                    "000.00000000..00"+
                    "00.0000000000.00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "000.000000000000"+
                    "0000........0000"+
                    "000000000000.000"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "00.0000000000.00"+
                    "00..00000000.000"+
                    "00..........0000"+
                    "0000000000000000";

    static public String charset_uprt =

                    "0000000000000000"+
                    "0..............0"+
                    "0..0000..0000..0"+
                    "0.00000..00000.0"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000....000000"+
                    "0000000000000000";

    static public String charset_upru =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000";

    static public String charset_uprv =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000.000000.0000"+
                    "00000.0000.00000"+
                    "000000.00.000000"+
                    "0000000..0000000"+
                    "0000000000000000";

    static public String charset_uprw =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.000....000.00"+
                    "00.0000..0000.00"+
                    "00.000.00.000.00"+
                    "00.00.0000.00.00"+
                    "00.0.000000.0.00"+
                    "00..00000000..00"+
                    "00.0000000000.00"+
                    "0000000000000000";

    static public String charset_uprx =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000.000000.0000"+
                    "00000.0000.00000"+
                    "000000.00.000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000.00.000000"+
                    "00000.0000.00000"+
                    "0000.000000.0000"+
                    "000.00000000.000"+
                    "00...000000...00"+
                    "0000000000000000";

    static public String charset_upry =

                    "0000000000000000"+
                    "0...00000000...0"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000.000000.0000"+
                    "00000.0000.00000"+
                    "000000.00.000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000....000000"+
                    "0000000000000000";

    static public String charset_uprz =
                    "0000000000000000"+
                    "00............00"+
                    "00..000000000.00"+
                    "000000000000.000"+
                    "00000000000.0000"+
                    "0000000000.00000"+
                    "000000000.000000"+
                    "00000000.0000000"+
                    "0000000.00000000"+
                    "000000.000000000"+
                    "00000.0000000000"+
                    "0000.00000000000"+
                    "000.000000000000"+
                    "00.000000000..00"+
                    "00............00"+
                    "0000000000000000";

    /* DIGITS */

    static public String charset_num_0 =


                    "0000000000000000"+
                    "000000....000000"+
                    "00000.0000.00000"+
                    "0000.000000.0000"+
                    "000.0000000..000"+
                    "00.0000000.00.00"+
                    "00.000000.000.00"+
                    "00.00000.0000.00"+
                    "00.0000.00000.00"+
                    "00.000.000000.00"+
                    "00.00.0000000.00"+
                    "000..0000000.000"+
                    "0000.000000.0000"+
                    "00000.0000.00000"+
                    "000000....000000"+
                    "0000000000000000";

    static public String charset_num_1 =


                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "000000...0000000"+
                    "00000....0000000"+
                    "0000..0..0000000"+
                    "000.000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000....000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_2 =


                    "0000000000000000"+
                    "0000000000000000"+
                    "00000.......0000"+
                    "000..0000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0000000000000.00"+
                    "00000000000..000"+
                    "000000000..00000"+
                    "0000000..0000000"+
                    "00000..000000000"+
                    "000..00000000000"+
                    "00..000000000000"+
                    "00............00"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_3 =


                    "0000000000000000"+
                    "00000.......0000"+
                    "000..0000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "0000000000000.00"+
                    "00000000000..000"+
                    "0000000....00000"+
                    "00000000000..000"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "000.000000000.00"+
                    "0000.0000000.000"+
                    "00000.......0000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_4 =


                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "000000..00000000"+
                    "00000..000000000"+
                    "0000..0000000000"+
                    "000..00..0000000"+
                    "00..000..0000000"+
                    "00..000..0000000"+
                    "00..000..0000000"+
                    "00...........000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "000000....000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_5 =


                    "0000000000000000"+
                    "00............00"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.........00000"+
                    "00000000000..000"+
                    "0000000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_6 =


                    "0000000000000000"+
                    "000000......0000"+
                    "0000..0000000000"+
                    "000.000000000000"+
                    "000.000000000000"+
                    "00.0000000000000"+
                    "00.0000000000000"+
                    "00.........00000"+
                    "00.00000000..000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_7 =
                    "0000000000000000"+
                    "00.............0"+
                    "00.0000000000.00"+
                    "0000000000000.00"+
                    "000000000000.000"+
                    "000000000000.000"+
                    "000000000.....00"+
                    "00000000000.0000"+
                    "0000000000.00000"+
                    "0000000000.00000"+
                    "000000000.000000"+
                    "000000000.000000"+
                    "00000000.0000000"+
                    "00000000.0000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_8 =


                    "0000000000000000"+
                    "0000........0000"+
                    "000.00000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000..000000..000"+
                    "00000......00000"+
                    "000..000000..000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_num_9 =
                    "0000000000000000"+
                    "0000........0000"+
                    "000.00000000.000"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "00.0000000000.00"+
                    "000..000000...00"+
                    "00000......00.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "0000000000000.00"+
                    "00.0000000000.00"+
                    "000.00000000.000"+
                    "0000........0000"+
                    "0000000000000000"+
                    "0000000000000000";

    /* PONCTUATION */

    static public String charset_ponc_dot =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000";


    static public String charset_ponc_comma =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "00000000.0000000"+
                    "00000000.0000000"+
                    "0000000000000000";


    static public String charset_ponc_interrog =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000.......0000"+
                    "0000.0000000.000"+
                    "000.000000000.00"+
                    "000.000000000.00"+
                    "0000..000000.000"+
                    "00000.0000..0000"+
                    "000000000.000000"+
                    "00000000.0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000";


    static public String charset_ponc_exclam =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000";


    static public String charset_ponc_semicol =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "00000000.0000000"+
                    "00000000.0000000"+
                    "0000000000000000";


    static public String charset_ponc_colon =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_ponc_space =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_ponc_quote =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_ponc_doublequote =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000..00..00000"+
                    "00000..00..00000"+
                    "00000..00..00000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";

    /* SPECIAL CHARS */

    static public String charset_spec_circumflex =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "00000......00000"+
                    "0000..0000..0000"+
                    "000..000000..000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_spec_underscore =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "................"+
                    "................"+
                    "0000000000000000";


    static public String charset_spec_slash =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000..0"+
                    "000000000000..00"+
                    "00000000000..000"+
                    "0000000000..0000"+
                    "000000000..00000"+
                    "00000000..000000"+
                    "0000000..0000000"+
                    "000000..00000000"+
                    "00000..000000000"+
                    "0000..0000000000"+
                    "000..00000000000"+
                    "00..000000000000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_antislash =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0..0000000000000"+
                    "00..000000000000"+
                    "000..00000000000"+
                    "0000..0000000000"+
                    "00000..000000000"+
                    "000000..00000000"+
                    "0000000..0000000"+
                    "00000000..000000"+
                    "000000000..00000"+
                    "0000000000..0000"+
                    "00000000000..000"+
                    "000000000000..00"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_hashtag =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000..0000..0000"+
                    "0000..0000..0000"+
                    "00............00"+
                    "00............00"+
                    "0000..0000..0000"+
                    "0000..0000..0000"+
                    "0000..0000..0000"+
                    "0000..0000..0000"+
                    "00............00"+
                    "00............00"+
                    "0000..0000..0000"+
                    "0000..0000..0000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_minus =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "00............00"+
                    "00............00"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_plus =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "00............00"+
                    "00............00"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_spec_amperstand =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000......000000"+
                    "000.000000.00000"+
                    "00.0000000.00000"+
                    "00.000000.000000"+
                    "000.0000.0000000"+
                    "0000...000....00"+
                    "000.000.000.0000"+
                    "00.00000.0.00000"+
                    "00.000000..00000"+
                    "00.00000.0.00000"+
                    "00..000.000.0000"+
                    "000....000....00"+
                    "0000000000000000"+
                    "0000000000000000";
    static public String charset_spec_at =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000......000"+
                    "00000..000000.00"+
                    "000..0000.....00"+
                    "00.0000...000.00"+
                    "00.000..00000.00"+
                    "00.000..00000.00"+
                    "00.000..00000.00"+
                    "00.000..0000..00"+
                    "00..000.000...00"+
                    "000.0000...0...0"+
                    "0000..0000000000"+
                    "000000........00"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_spec_dollar =

                    "0000000000000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000.........000"+
                    "000.000..000..00"+
                    "00.0000..0000000"+
                    "000.000..0000000"+
                    "0000........0000"+
                    "0000000..000.000"+
                    "0000000..0000.00"+
                    "0000000..0000.00"+
                    "00..000..000.000"+
                    "000.........0000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000000000000";

    static public String charset_spec_modulo =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000.00"+
                    "000000000000..00"+
                    "00000000000..000"+
                    "000...0000..0000"+
                    "000...000..00000"+
                    "00000000..000000"+
                    "0000000..0000000"+
                    "000000..00000000"+
                    "00000..000...000"+
                    "0000..0000...000"+
                    "000..00000000000"+
                    "00..000000000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_spec_asterix =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00.0000..0000.00"+
                    "00..000..000..00"+
                    "000..00..00..000"+
                    "0000..0..0..0000"+
                    "00000......00000"+
                    "00............00"+
                    "00............00"+
                    "00000......00000"+
                    "0000..0..0..0000"+
                    "000..00..00..000"+
                    "00..000..000..00"+
                    "00.0000..0000.00"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_pipe =

                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000"+
                    "0000000..0000000";


    static public String charset_spec_graveacc =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000..000000000"+
                    "000000..00000000"+
                    "0000000..0000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";



    static public String charset_spec_open_parenthese =

                    "0000000000000000"+
                    "0000000000000000"+
                    "000000000..00000"+
                    "00000000..000000"+
                    "0000000..0000000"+
                    "000000..00000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "000000..00000000"+
                    "0000000..0000000"+
                    "00000000..000000"+
                    "000000000..00000"+
                    "0000000000000000"+
                    "0000000000000000";



    static public String charset_spec_close_parenthese =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000..000000000"+
                    "000000..00000000"+
                    "0000000..0000000"+
                    "00000000..000000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "00000000..000000"+
                    "0000000..0000000"+
                    "000000..00000000"+
                    "00000..000000000"+
                    "0000000000000000";


    static public String charset_spec_open_bracket =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000......00000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000......00000"+
                    "0000000000000000"+
                    "0000000000000000";



    static public String charset_spec_close_bracket =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000......00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "00000......00000"+
                    "0000000000000000";

    static public String charset_spec_open_brace =

                    "0000000000000000"+
                    "0000000000000000"+
                    "000000.....00000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "000...0000000000"+
                    "000...0000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "00000..000000000"+
                    "000000.....00000"+
                    "0000000000000000"+
                    "0000000000000000";



    static public String charset_spec_close_brace =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000.....000000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "0000000000...000"+
                    "0000000000...000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "000000000..00000"+
                    "00000.....000000"+
                    "0000000000000000";


    static public String charset_spec_equal =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "00............00"+
                    "00............00"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "00............00"+
                    "00............00"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_spec_tilde =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000...00000..00"+
                    "000.....0000..00"+
                    "00..0000.....000"+
                    "00..00000...0000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000"+
                    "0000000000000000";



    static public String charset_spec_lower =

                    "0000000000000000"+
                    "0000000000000000"+
                    "00000000000...00"+
                    "000000000...0000"+
                    "0000000...000000"+
                    "00000...00000000"+
                    "000...0000000000"+
                    "0...000000000000"+
                    "0...000000000000"+
                    "000...0000000000"+
                    "00000...00000000"+
                    "0000000...000000"+
                    "000000000...0000"+
                    "00000000000...00"+
                    "0000000000000000"+
                    "0000000000000000";


    static public String charset_spec_greater =

                    "0000000000000000"+
                    "0000000000000000"+
                    "0...000000000000"+
                    "000...0000000000"+
                    "00000...00000000"+
                    "0000000...000000"+
                    "000000000...0000"+
                    "00000000000...00"+
                    "00000000000...00"+
                    "000000000...0000"+
                    "0000000...000000"+
                    "00000...00000000"+
                    "000...0000000000"+
                    "0...000000000000"+
                    "0000000000000000"+
                    "0000000000000000";

    static public String charset_default_tile =

                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0"+
                    "0.0.0.0.0.0.0.0."+
                    ".0.0.0.0.0.0.0.0";

}