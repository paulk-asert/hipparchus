/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hipparchus.special.elliptic;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.Field;
import org.hipparchus.complex.FieldComplex;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.util.Decimal64Field;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.junit.Assert;
import org.junit.Test;

public class FieldJacobiThetaTest {

    @Test
    public void testNoConvergence() {
        doTestNoConvergence(Decimal64Field.getInstance());
    }

    private <T extends CalculusFieldElement<T>> void doTestNoConvergence(Field<T> field) {
        try {
            new FieldJacobiTheta<>(field.getZero().newInstance(Double.NaN)).values(FieldComplex.getZero(field));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalStateException mise) {
            Assert.assertEquals(LocalizedCoreFormats.CONVERGENCE_FAILED, mise.getSpecifier());
        }
    }

    @Test
    public void testRealZero() {
        doTestRealZero(Decimal64Field.getInstance());
    }

    private <T extends CalculusFieldElement<T>> void doTestRealZero(Field<T> field) {
        final FieldLegendreEllipticIntegral<T> ei     = new FieldLegendreEllipticIntegral<>(field.getZero().newInstance(0.675));
        final T                        q      = ei.getNome();
        final T                        t3Ref  = field.getOne().add(q.
                                                                   add(FastMath.pow(q, 4)).
                                                                   add(FastMath.pow(q, 9)).
                                                                   add(FastMath.pow(q, 16)).multiply(2));
        final T                        theta3 = new FieldJacobiTheta<>(q).values(FieldComplex.getZero(field)).theta3().getRealPart();
        Assert.assertEquals(t3Ref.getReal(), theta3.getReal(), 1.0e-12);
    }

    @Test
    public void testQuarterPeriod() {
        doTestQuarterPeriod(Decimal64Field.getInstance());
    }

    private <T extends CalculusFieldElement<T>> void doTestQuarterPeriod(Field<T> field) {
        final FieldLegendreEllipticIntegral<T> ei     = new FieldLegendreEllipticIntegral<>(field.getZero().newInstance(0.675));
        final T                        q      = ei.getNome();
        final T                        theta3 = new FieldJacobiTheta<>(q).values(FieldComplex.getZero(field)).theta3().getRealPart();
        Assert.assertEquals(ei.getBigK().getReal(),
                            theta3.multiply(theta3).multiply(MathUtils.SEMI_PI).getReal(),
                            1.0e-12);
    }

    @Test
    public void testEllipticFunctions() {
        doTestEllipticFunctions(Decimal64Field.getInstance());
    }

    private <T extends CalculusFieldElement<T>> void doTestEllipticFunctions(Field<T> field) {

        final T z = field.getZero().newInstance(1.3);
        final FieldLegendreEllipticIntegral<T> ei     = new FieldLegendreEllipticIntegral<>(field.getZero().newInstance(0.675));
        final T                        zeta   = z.divide(ei.getBigK()).multiply(MathUtils.SEMI_PI);
        final FieldJacobiTheta<T>      jt     = new FieldJacobiTheta<>(ei.getNome());
        final FieldTheta<T>            theta0 = jt.values(FieldComplex.getZero(field));
        final FieldTheta<T>            thetaZ = jt.values(new FieldComplex<>(zeta));

        // the theta functions are related to the elliptic functions
        // see https://dlmf.nist.gov/22.2
        final FieldJacobiElliptic<T> je = JacobiEllipticBuilder.build(ei.getK().multiply(ei.getK()));
        final FieldCopolarN<T> valuesN = je.valuesN(z);
        final FieldCopolarD<T> valuesD = je.valuesD(z);
        final FieldCopolarC<T> valuesC = je.valuesC(z);
        final T t02 = theta0.theta2().getRealPart();
        final T t03 = theta0.theta3().getRealPart();
        final T t04 = theta0.theta4().getRealPart();
        final T tz1 = thetaZ.theta1().getRealPart();
        final T tz2 = thetaZ.theta2().getRealPart();
        final T tz3 = thetaZ.theta3().getRealPart();
        final T tz4 = thetaZ.theta4().getRealPart();
        Assert.assertEquals(valuesN.sn().getReal(), t03.multiply(tz1)              .divide(t02.multiply(tz4)).getReal(),               1.0e-15);
        Assert.assertEquals(valuesN.cn().getReal(), t04.multiply(tz2)              .divide(t02.multiply(tz4)).getReal(),               1.0e-15);
        Assert.assertEquals(valuesN.dn().getReal(), t04.multiply(tz3)              .divide(t03.multiply(tz4)).getReal(),               1.0e-15);
        Assert.assertEquals(valuesD.sd().getReal(), t03.multiply(t03).multiply(tz1).divide(t02.multiply(t04).multiply(tz3)).getReal(), 1.0e-15);
        Assert.assertEquals(valuesD.cd().getReal(), t03.multiply(tz2)              .divide(t02.multiply(tz3)).getReal(),               1.0e-15);
        Assert.assertEquals(valuesC.sc().getReal(), t03.multiply(tz1)              .divide(t04.multiply(tz2)).getReal(),               1.0e-15);

    }

}