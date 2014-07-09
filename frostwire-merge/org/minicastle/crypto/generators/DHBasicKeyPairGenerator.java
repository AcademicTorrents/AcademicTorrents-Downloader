package org.minicastle.crypto.generators;

import java.math.BigInteger;

import org.minicastle.crypto.AsymmetricCipherKeyPair;
import org.minicastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.minicastle.crypto.KeyGenerationParameters;
import org.minicastle.crypto.params.DHKeyGenerationParameters;
import org.minicastle.crypto.params.DHParameters;
import org.minicastle.crypto.params.DHPrivateKeyParameters;
import org.minicastle.crypto.params.DHPublicKeyParameters;

/**
 * a basic Diffie-Helman key pair generator.
 *
 * This generates keys consistent for use with the basic algorithm for
 * Diffie-Helman.
 */
public class DHBasicKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private DHKeyGenerationParameters param;

    public void init(
        KeyGenerationParameters param)
    {
        this.param = (DHKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        BigInteger      p, g, x, y;
        int             qLength = param.getStrength() - 1;
        DHParameters    dhParams = param.getParameters();

        p = dhParams.getP();
        g = dhParams.getG();
   
        //
        // calculate the private key
        //
		x = new BigInteger(qLength, param.getRandom());
		
        //
        // calculate the public key.
        //
        y = g.modPow(x, p);

        return new AsymmetricCipherKeyPair(
                new DHPublicKeyParameters(y, dhParams),
                new DHPrivateKeyParameters(x, dhParams));
    }
}
