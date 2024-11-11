package com.rubix.rubix;

// Lazy, Not optimized
public class MengerSponge_lazy {

    // private or public
    // If all is public when compiled ('flat compilation')
    // private:
    // Useful for coders
    // public:
    // So easy to crack
    // If all is mixed (public/private)
    // public/private:
    // Tells whats is much more interesting for coders
    // Comments: Disapear, because public private _ act as comments
    private MengerSponge_lazy voxel[];

    public void onCreate(int depth)
    {
        if (depth-- == 0)
            return ;
        voxel = new MengerSponge_lazy[27];
        /*
        voxel[0] = MengerSponge_lazy(depth - 1);
        voxel[1] = MengerSponge_lazy(depth - 1);
        voxel[2] = MengerSponge_lazy(depth - 1);
        voxel[3] = MengerSponge_lazy(depth - 1);
        voxel[4] = MengerSponge_lazy(depth - 1);
        voxel[5] = MengerSponge_lazy(depth - 1);
        voxel[6] = MengerSponge_lazy(depth - 1);
        */
    }

    /*
    public void render(int )
    {
        ;
    }*/
}
